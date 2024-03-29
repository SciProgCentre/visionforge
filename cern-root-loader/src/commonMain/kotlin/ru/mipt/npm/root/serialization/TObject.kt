package ru.mipt.npm.root.serialization

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public abstract class TObject {
    public val fUniqueID: UInt = 0u
    public val fBits: UInt = 0u
}

@Serializable
public open class TNamed : TObject() {
    public val fName: String = ""
    public val fTitle: String = ""
}


@Serializable
@SerialName("TObjArray")
public class TObjArray<T: TObject>(public val arr: List<@Contextual T>): TObject() {
    public companion object{
        public fun <T: TObject> getEmpty(): TObjArray<T> = TObjArray(emptyList())
    }
}

@Serializable
@SerialName("TList")
public class TList(public val arr: List<@Contextual TObject>): TObject()

@Serializable
@SerialName("THashList")
public class THashList(public val arr: List<@Contextual TObject>): TObject()