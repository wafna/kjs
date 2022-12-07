import csstype.ClassName
import csstype.Cursor
import emotion.react.css
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.em
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.small
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.strong

external interface RecordEditorProps : Props {
    var record: Record?
    var updateRecord: (Record) -> Unit
    var createRecord: (RecordWIP) -> Unit
}

val RecordEditor = FC<RecordEditorProps> { props ->
    val record = props.record

    var data by useState(record?.data ?: "")

    form {
        div {
            val ctrlId = "the-data"
            css(ClassName("form-group"))
            label {
                +"Data"
                htmlFor = ctrlId
            }
            input {
                id = ctrlId
                css(ClassName("form-control"))
                placeholder = "..."
                value = data
                onChange = { e ->
                    data = e.target.value
                }
            }
        }
        div {
            if (null == record) {
                button {
                    css(ClassName("btn btn-primary"))
                    +"Create"
                    onClick = preventDefault {
                        props.createRecord(RecordWIP(data))
                    }
                }
            } else {
                button {
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
        else -> div {
            div {
                css(ClassName("container"))
                fun col(n: Int) = "col-lg-$n"
                div {
                    css(ClassName("row"))
                    div {
                        css(ClassName(col(1)))
                        small { +"delete" }
                    }
                    div {
                        css(ClassName(col(1)))
                        small { +"modify" }
                    }
                    div {
                        css(ClassName(col(5)))
                        strong { +"Id" }
                    }
                    div {
                        css(ClassName(col(5)))
                        strong { +"Data" }
                    }
                }
                records!!.forEach { record ->
                    val id = record.id
                    div {
                        css(ClassName("row"))
                        div {
                            css(ClassName(col(1)))
                            key = id
                            span {
                                css {
                                    cursor = Cursor.pointer
                                }
                                +"∄"
                                onClick = preventDefault {
                                    mainScope.launch {
                                        API.deleteRecord(id)
                                        updateList()
                                    }
                                }
                            }
                        }
                        div {
                            css(ClassName(col(1)))
                            span {
                                css {
                                    cursor = Cursor.pointer
                                }
                                +"∆"
                                onClick = preventDefault {
                                    editedRecord = record
                                }
                            }
                        }
                        div {
                            css(ClassName(col(5)))
                            span { +record.id }
                        }
                        div {
                            css(ClassName(col(5)))
                            span { +record.data }
                        }
                    }
                }
            }
            button {
                css(ClassName("btn btn-primary"))
                em { +"+" }
                onClick = { e ->
                    e.preventDefault()
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

