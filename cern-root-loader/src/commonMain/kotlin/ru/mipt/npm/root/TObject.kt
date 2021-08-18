package ru.mipt.npm.root

import kotlinx.serialization.Serializable

@Serializable
public abstract class TObject {
    public val fUniqueID: UInt = 0u
    public val fBits: UInt = 0u
}

@Serializable
public abstract class TNamed : TObject() {
    public val fName: String = ""
    public val fTitle: String = ""
}

@Serializable
public class TObjArray(public val arr: List<TObject>){
    public companion object{
        public val empty = TObjArray(emptyList())
    }
}