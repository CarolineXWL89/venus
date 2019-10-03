package venus.zip

import venus.vfs.VFSFile
import venus.vfs.VFSFolder
import venus.vfs.VFSObject
import venus.vfs.VFSType

class Zip {
    var internal_zip = JSZip()
    fun addFile(name: String, data: Any) {
        this.addFileHelper(name, data, internal_zip)
    }

    private fun addFileHelper(name: String, data: Any, int_zip: JSZip) {
        int_zip.file(name, data, js("""{"binary":true}"""))
    }

    fun save(name: String): String {
        val z = internal_zip
        js("""
           z.generateAsync({"type": "blob"}).then(function(data){
            saveAs(data, name);
           });
        """)
        return ""
    }

    fun addFolder(folder: VFSFolder) {
        val newf = internal_zip.folder(folder.name)
        this.addFolderHelper(folder, newf)
    }

    private fun addFolderHelper(folder: VFSFolder, int_zip: JSZip) {
        for (s in folder.contents.keys) {
            if (s !in listOf(".", "..")) {
                val type = (folder.contents[s] as VFSObject).type
                if (type == VFSType.File) {
                    val file = folder.contents[s] as VFSFile
                    this.addFileHelper(file.label, file.readText(), int_zip)
                } else if (type == VFSType.Folder) {
                    val fold = folder.contents[s] as VFSFolder
                    val newf = int_zip.folder(fold.name)
                    this.addFolderHelper(fold, newf)
                } else {
                    console.error("Currently, we only support zipping files and folders!")
                }
            }
        }
    }
}

external class JSZip {
    companion object {
        val version: String
    }
    fun file(name: String): JSZip
    fun file(name: String, data: Any, options: Any): JSZip
    fun folder(name: String): JSZip
    fun remove(name: String): JSZip
}