package com.axmp3

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.Locale

class FolderAdapter(
private val context: Context,
private var originalFolderList: List<FolderModel>,
private val listener: OnFolderClickListener
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

private var displayedFolderList = ArrayList<FolderModel>()

init {
displayedFolderList.addAll(originalFolderList)
}

interface OnFolderClickListener {
fun onFolderClick(folderName: String, songs: List<Song>)
}

fun filter(text: String) {
val query = text.lowercase(Locale.getDefault()).trim()
displayedFolderList.clear()

if (query.isEmpty()) {
displayedFolderList.addAll(originalFolderList)
} else {
for (folder in originalFolderList) {
if (folder.name.lowercase(Locale.getDefault()).contains(query)) {
displayedFolderList.add(folder)
}
}
}
notifyDataSetChanged()
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
val itemView = FolderItemView(context)
return FolderViewHolder(itemView)
}

override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
val folder = displayedFolderList[position]

// 🛠️ تم الإصلاح بالوصول عبر الـ view (holder.view) مباشرة ليتعرف عليها الـ Compiler
holder.view.folderNameTxt.text = folder.name
holder.view.songCountTxt.text = "${folder.songs.size} أغنية"

holder.view.rootView.setOnClickListener {
val currentPos = holder.adapterPosition
if (currentPos != RecyclerView.NO_POSITION) {
val currentFolder = displayedFolderList[currentPos]
listener.onFolderClick(currentFolder.name, currentFolder.songs)
}
}
}

override fun getItemCount(): Int = displayedFolderList.size

fun updateData(newList: List<FolderModel>) {
this.originalFolderList = newList
this.displayedFolderList.clear()
this.displayedFolderList.addAll(newList)
notifyDataSetChanged()
}

// 🛠️ ربط الـ Holder بـ FolderItemView باسم حقل مخصص لعدم التعارض مع itemView الافتراضية للأندرويد
class FolderViewHolder(val view: FolderItemView) : RecyclerView.ViewHolder(view.rootView)
}

data class FolderModel(
val name: String,
val songs: List<Song>
)