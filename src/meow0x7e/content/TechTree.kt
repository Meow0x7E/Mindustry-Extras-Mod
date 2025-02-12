package meow0x7e.content

import arc.Events
import arc.util.Log
import mindustry.content.TechTree.*
import mindustry.ctype.UnlockableContent
import mindustry.game.EventType
import mindustry.content.Blocks as MindustryBlocks
import mindustry.content.Planets as MindustryPlanets

class TechTree {
    companion object {
        /**
         * 在 [TechNode] 中查找特定的节点 [content]，并执行指定的操作 [lambda]。
         * 如果找到匹配的节点，则将其作为参数传递给 [lambda] 并返回。
         *
         * @param content 要查找的 [UnlockableContent]
         * @param lambda 匹配节点时要执行的操作
         */
        @JvmStatic
        fun TechNode.findNode(content: UnlockableContent, lambda: (TechNode) -> Unit) {
            this.children.forEach {
                when {
                    it.content.name == content.name -> {
                        lambda(it)
                        return
                    }

                    it.children.any() -> it.findNode(content, lambda)
                }
            }
        }

        /**
         * 在 [TechNode] 中查找特定的节点 [content]，然后将给定的节点 [nodes] 添加为其子节点。
         *
         * @param content 要查找的 [UnlockableContent]
         * @param nodes 要添加为子节点的 [TechNode] 实例
         */
        @JvmStatic
        fun TechNode.findAndAddNode(content: UnlockableContent, vararg nodes: TechNode) {
            this.findNode(content) { nodes.forEach(it.children::add) }
        }

        /**
         * 在 [TechNode] 中查找特定的节点 [content]，然后将给定的 [contents] 创建的节点添加为其子节点。
         *
         * @param content 要查找的 [UnlockableContent]
         * @param contents 要作为子节点添加的 [UnlockableContent] 实例
         */
        @JvmStatic
        fun TechNode.findAndAddNode(content: UnlockableContent, vararg contents: UnlockableContent) {
            val nodes = ArrayList<TechNode>()
            contents.forEach { nodes.add(node(it)) }
            this.findAndAddNode(content, *nodes.toTypedArray())
        }

        /**
         * 同步解锁指定内容。
         *
         * 当 [checkContent] 解锁时，自动解锁 [unlockContents] 中的所有内容。如果 [checkContent] 已经解锁，则在客户端首次加载完成时解锁 [unlockContents] 中未解锁的内容。
         *
         * @param checkContent 需要被检查是否解锁的内容
         * @param unlockContents 当 [checkContent] 解锁时，需要同步解锁的内容列表
         */
        @JvmStatic
        fun syncUnlocks(checkContent: UnlockableContent,vararg unlockContents: UnlockableContent) {
            if (!checkContent.unlocked()) {
                Log.debug("监听事件 EventType.UnlockEvent，当 ${checkContent.name} 解锁时同步解锁 ${unlockContents.joinToString("、")}")
                Events.on(EventType.UnlockEvent::class.java) { event ->
                    if (event.content == checkContent) unlockContents.forEach { it.unlock() }
                }
                return
            }
            Log.debug("监听事件 EventType.ClientLoadEvent，当客户端首次加载完成时同步解锁 ${unlockContents.joinToString("、")}")
            Events.on(EventType.ClientLoadEvent::class.java) {
                unlockContents.forEach { if (!it.unlocked()) it.unlock() }
            }
        }

        fun load() {
            with(MindustryPlanets.serpulo.techTree) {
                findAndAddNode(
                    MindustryBlocks.mechanicalDrill,
                    Blocks.mechanicalDrillSmall,
                    Blocks.mechanicalDrillLarge,
                    Blocks.mechanicalDrillExtraLarge
                )
                findAndAddNode(
                    MindustryBlocks.pneumaticDrill,
                    Blocks.pneumaticDrillSmall,
                    Blocks.pneumaticDrillLarge,
                    Blocks.pneumaticDrillExtraLarge
                )
            }
        }
    }
}