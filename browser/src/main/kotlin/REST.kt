import csstype.ClassName
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML as h

external interface RecordEditorProps : Props {
    var record: Record?
    var updateRecord: (Record) -> Unit
    var createRecord: (RecordWIP) -> Unit
}

val RecordEditor = FC<RecordEditorProps> { props ->

    val record = props.record
    var data by useState(record?.data ?: "")

    h.form {
        h.div {
            css(ClassName("form-group"))
            val ctrlId = "the-data"
            h.label {
                +"Data"
                htmlFor = ctrlId
            }
            h.input {
                css(ClassName("form-control"))
                id = ctrlId
                placeholder = "..."
                value = data
                onChange = { data = it.target.value }
            }
        }
        h.div {
            if (null == record) {
                h.button {
                    css(ClassName("btn btn-primary"))
                    +"Create"
                    onClick = preventDefault {
                        props.createRecord(RecordWIP(data))
                    }
                }
            } else {
                h.button {
                    css(classNames("btn", "btn-primary"))
                    +"Update"
                    onClick = preventDefault {
                        props.updateRecord(Record(record.id, data))
                    }
                }
            }
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
                css(ClassName("container"))
                fun col(n: Int) = "col-lg-$n"
                h.div {
                    css(ClassName("row"))
                    h.div {
                        css(ClassName(col(1)))
                        h.small { +"delete" }
                    }
                    h.div {
                        css(ClassName(col(1)))
                        h.small { +"modify" }
                    }
                    h.div {
                        css(ClassName(col(5)))
                        h.strong { +"Id" }
                    }
                    h.div {
                        css(ClassName(col(5)))
                        h.strong { +"Data" }
                    }
                }
                records!!.forEach { record ->
                    val id = record.id
                    h.div {
                        css(ClassName("row"))
                        h.div {
                            css(ClassName(col(1)))
                            key = id
                            h.span {
                                css(ClassName("clickable"))
                                +"∄"
                                onClick = preventDefault {
                                    mainScope.launch {
                                        API.deleteRecord(id)
                                        updateList()
                                    }
                                }
                            }
                        }
                        h.div {
                            css(ClassName(col(1)))
                            h.span {
                                css(ClassName("clickable"))
                                +"∆"
                                onClick = preventDefault {
                                    editedRecord = record
                                }
                            }
                        }
                        h.div {
                            css(ClassName(col(5)))
                            h.span { +record.id }
                        }
                        h.div {
                            css(ClassName(col(5)))
                            h.span { +record.data }
                        }
                    }
                }
            }
            h.button {
                css(ClassName("btn btn-primary"))
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

