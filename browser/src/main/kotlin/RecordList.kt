import csstype.ClassName
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML

external interface RecordEditorProps : Props {
    var record: Record?
    var updateRecord: (Record) -> Unit
    var createRecord: (String) -> Unit
}

val RecordEditor = FC<RecordEditorProps> { props ->
    val record = props.record

    var data by useState(record?.data ?: "")

    ReactHTML.form {
        ReactHTML.div {
            val ctrlId = "the-data"
            css(ClassName("form-group"))
            ReactHTML.label {
                +"Data"
                htmlFor = ctrlId
            }
            ReactHTML.input {
                id = ctrlId
                css(ClassName("form-control"))
                placeholder = "..."
                value = data
                onChange = { e ->
                    data = e.target.value
                }
            }
        }
        ReactHTML.div {
            ReactHTML.button {
                css(ClassName("btn btn-primary"))
                if (null == record) {
                    +"Create"
                    onClick = {
                        it.preventDefault()
                        props.createRecord(data)
                    }
                } else {
                    +"Update"
                    onClick = {
                        it.preventDefault()
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
        else -> ReactHTML.div {
            ReactHTML.div {
                css(ClassName("container"))
                fun col(n: Int) = "col-lg-$n"
                ReactHTML.div {
                    css(ClassName("row"))
                    ReactHTML.div {
                        css(ClassName(col(1)))
                    }
                    ReactHTML.div {
                        css(ClassName(col(1)))
                    }
                    ReactHTML.div {
                        css(ClassName(col(5)))
                        ReactHTML.span { +"Id" }
                    }
                    ReactHTML.div {
                        css(ClassName(col(5)))
                        ReactHTML.span { +"Data" }
                    }
                }
                records!!.forEach { record ->
                    val id = record.id
                    ReactHTML.div {
                        css(ClassName("row"))
                        ReactHTML.div {
                            css(ClassName(col(1)))
                            key = id
                            ReactHTML.span {
                                +"❌"
                                onClick = {
                                    mainScope.launch {
                                        API.deleteRecord(id)
                                        updateList()
                                    }
                                }
                            }
                        }
                        ReactHTML.div {
                            css(ClassName(col(1)))
                            ReactHTML.span {
                                +"\uD83D\uDD89"
                                onClick = {
                                    editedRecord = record
                                }
                            }
                        }
                        ReactHTML.div {
                            css(ClassName(col(5)))
                            ReactHTML.span { +record.id }
                        }
                        ReactHTML.div {
                            css(ClassName(col(5)))
                            ReactHTML.span { +record.data }
                        }
                    }
                }
            }
            if (null != editedRecord) {
                RecordEditor {
                    record = editedRecord
                    updateRecord = { record ->
                        mainScope.launch {
                            console.log("update record", record)
                            API.updateRecord(record)
                            editedRecord = null
                        }
                    }
                }
            }
        }
    }
}

