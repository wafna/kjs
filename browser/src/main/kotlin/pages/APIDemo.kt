package pages

import API
import Loading
import Record
import RecordWIP
import csstype.ClassName
import emotion.react.css
import kotlinx.coroutines.launch
import mainScope
import react.*
import react.dom.events.MouseEvent
import util.PropsSplat
import util.classNames
import util.preventDefault
import util.withTargetValue
import react.dom.html.ReactHTML as h

private external interface RecordEditorProps : Props {
    var record: Record?
    var updateRecord: (Record) -> Unit
    var createRecord: (RecordWIP) -> Unit
}

private val RecordEditor = FC<RecordEditorProps> { props ->

    val record = props.record
    var data by useState(record?.data ?: "")

    h.form {
        h.div {
            css(ClassName("form-group")) {}
            val ctrlId = "the-data"
            h.label {
                +"Data"
                htmlFor = ctrlId
            }
            h.input {
                css(ClassName("form-control")) {}
                id = ctrlId
                placeholder = "..."
                value = data
                onChange = withTargetValue { data = it }
            }
        }
        h.div {
            if (null == record) {
                h.button {
                    css(ClassName("btn btn-primary")) {}
                    +"Create"
                    onClick = preventDefault {
                        props.createRecord(RecordWIP(data))
                    }
                }
            } else {
                h.button {
                    css(classNames("btn", "btn-primary")) {}
                    +"Update"
                    onClick = preventDefault {
                        props.updateRecord(Record(record.id, data))
                    }
                }
            }
        }
    }
}

private external interface ItemCtrlProps : PropsSplat {
    var onClick: (MouseEvent<*, *>) -> Unit
}

private fun col(n: Int) = "col-lg-$n"

private val ItemCtrl = FC<ItemCtrlProps> { props ->
    h.div {
        css(ClassName(col(1))) {}
        h.span {
            css(ClassName("clickable")) {}
            children = props.children
            onClick = preventDefault { props.onClick(it) }
        }
    }
}

val RecordList = FC<Props> {

    var records: List<Record>? by useState(null)
    var editedRecord: Record? by useState(null)
    var createNew by useState(false)

    suspend fun updateList() {
        records = API.listRecords()
    }

    useEffectOnce {
        mainScope.launch {
            updateList()
        }
    }

    when (records) {
        null -> Loading
        else -> h.div {
            h.div {
                css(ClassName("container")) {}
                h.div {
                    css(ClassName("row")) {}
                    h.div {
                        css(ClassName(col(1))) {}
                        h.small { +"delete" }
                    }
                    h.div {
                        css(ClassName(col(1))) {}
                        h.small { +"modify" }
                    }
                    h.div {
                        css(ClassName(col(5))) {}
                        h.strong { +"Id" }
                    }
                    h.div {
                        css(ClassName(col(5))) {}
                        h.strong { +"Data" }
                    }
                }
                records!!.forEach { record ->
                    val id = record.id
                    h.div {
                        key = id
                        css(ClassName("row")) {}
                        ItemCtrl {
                            +"∄"
                            onClick = preventDefault {
                                mainScope.launch {
                                    API.deleteRecord(id)
                                    updateList()
                                }
                            }
                        }
                        ItemCtrl {
                            +"∆"
                            onClick = preventDefault {
                                editedRecord = record
                            }
                        }
                        h.div {
                            css(ClassName(col(5))) {}
                            h.pre { +record.id }
                        }
                        h.div {
                            css(ClassName(col(5))) {}
                            h.span { +record.data }
                        }
                    }
                }
            }
            h.button {
                css(ClassName("btn btn-primary")) {}
                h.em { +"+" }
                onClick = preventDefault {
                    createNew = true
                }
            }

            if (null != editedRecord) {
                RecordEditor {
                    record = editedRecord
                    updateRecord = { record ->
                        mainScope.launch {
                            API.updateRecord(record)
                            updateList()
                            editedRecord = null
                        }
                    }
                }
            } else if (createNew) {
                RecordEditor {
                    record = null
                    createRecord = { record ->
                        mainScope.launch {
                            API.createRecord(record)
                            updateList()
                            createNew = false
                        }
                    }
                }
            }
        }
    }
}

