package com.bhuvaneshw.pdf.model

import kotlinx.serialization.Serializable

/**
 * Represents an item in the sidebar tree, which can be an outline item or an attachment.
 *
 * @property title The title of the item.
 * @property dest The destination of the item. The link of outline item or attachment.
 * @property children A list of sub-items, for building a tree structure.
 * @property id A unique identifier for the item.
 */
@Serializable
data class SideBarTreeItem(
    val title: String?,
    val dest: String?,
    val children: List<SideBarTreeItem>,
    val id: String,
)
