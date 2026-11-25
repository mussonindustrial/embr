package com.mussonindustrial.ignition.embr.periscope.component

import javax.swing.tree.DefaultMutableTreeNode

fun DefaultMutableTreeNode.childNodes(): List<DefaultMutableTreeNode> {
    return children().asSequence().filterIsInstance<DefaultMutableTreeNode>().toList()
}

fun DefaultMutableTreeNode.addChild(userObject: Any): DefaultMutableTreeNode {
    val node = DefaultMutableTreeNode(userObject)
    add(node)
    return node
}
