package com.app.samplewearapp.util



class CustomViewType {

    interface ViewTypeData {
        val type: Int
    }

    class TypeDefault : ViewTypeData {
        override val type: Int
            get() = TYPE_DEFAULT
    }


    class TypeOptions : ViewTypeData {
        override val type: Int
            get() = TYPE_OPTIONS
    }

    companion object{
        const val TYPE_DEFAULT = 0
        const val TYPE_OPTIONS = 1
    }
}