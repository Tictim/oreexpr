import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gregtech.api.util.oreglob.OreGlobCompileResult
import gregtech.common.covers.filter.OreDictExprFilter
import gregtech.common.covers.filter.oreglob.impl.OreGlobParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App() {
    var expression by remember { mutableStateOf("") }
    var parsedOld by remember { mutableStateOf(listOf<OreDictExprFilter.MatchRule>()) }
    var parsedNew by remember { mutableStateOf<OreGlobCompileResult?>(null) }
    var input by remember { mutableStateOf("") }

    var testResultOld by remember { mutableStateOf("") }
    var testResultNew by remember { mutableStateOf("") }
    var compileResult by remember { mutableStateOf("") }
    var logs by remember { mutableStateOf("") }
    var perfTestOld by remember { mutableStateOf("") }
    var perfTestNew by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var currentJob by remember { mutableStateOf<Job?>(null) }
    val increment by remember { mutableStateOf(AtomicInteger()) }

    fun updateMatch() {
        testResultOld =
            if (parsedOld.isEmpty() || input.isEmpty()) ""
            else try {
                if (OreDictExprFilter.matches(parsedOld, input)) "Matches"
                else "Doesn't match"
            } catch (ex: Exception) {
                "Crashes lol"
            }
        testResultNew =
            if (parsedOld.isEmpty() || input.isEmpty()) ""
            else parsedNew?.instance?.matches(input)?.let {
                if (it) "Matches"
                else "Doesn't match"
            } ?: ""

        compileResult = parsedNew?.instance?.toString() ?: ""
        logs = parsedNew?.reports?.joinToString("\n") { it.toString() } ?: ""

        if (input.isNotEmpty() && parsedOld.isNotEmpty() && parsedNew != null) {
            val result = parsedNew!!
            if (result.reports.none { it.isError }) {
                val inputCache = input
                currentJob?.cancel()
                val currentIncrement = increment.incrementAndGet()
                currentJob = scope.launch {
                    delay(500)
                    if (currentIncrement == increment.get()) {
                        val dOld = try {
                            measureTime {
                                (1..1000).map {
                                    OreDictExprFilter.matches(parsedOld, inputCache)
                                }.toList()
                            }
                        } catch (ex: Exception) {
                            null
                        }
                        val dNew = measureTime {
                            (1..1000).map {
                                result.instance.matches(inputCache)
                            }.toList()
                        }
                        perfTestOld = dOld?.toString() ?: "Crashes lol"
                        perfTestNew = dNew.toString()
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.background(Color.LightGray).padding(5.dp)) {
        Text(text = "Ore filter", color = Color.DarkGray)
        TextField(
            expression,
            onValueChange = {
                expression = it
                parsedOld = OreDictExprFilter.parseExpression(it)
                parsedNew = if (it.isEmpty()) null else OreGlobParser(it).compile()
                updateMatch()
            },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Text to match", color = Color.DarkGray)
        TextField(
            input,
            onValueChange = {
                input = it
                updateMatch()
            },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .padding(5.dp)
            ) {
                Text(text = "Previous filter impl:", color = Color.DarkGray, fontSize = 20.sp)
                Text(testResultOld)
                Text(text = "Performance test (1,000 calls):", color = Color.DarkGray, fontSize = 20.sp)
                Text(perfTestOld)
            }

            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .padding(5.dp)
            ) {
                Text(text = "Ore expression nouveau:", color = Color.DarkGray, fontSize = 20.sp)
                Text(testResultNew)
                Text(text = "Performance test (1,000 calls):", color = Color.DarkGray, fontSize = 20.sp)
                Text(perfTestNew)

                val scroll = rememberScrollState(0)

                Text(text = "Compilation result:", color = Color.DarkGray, fontSize = 20.sp)
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(state = scroll)
                    ) {
                        Text(compileResult)
                        Text(text = "Logs:", color = Color.DarkGray, fontSize = 20.sp)
                        Text(logs)
                    }
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(scroll),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                    )
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ore Expression Test"
    ) {
        App()
    }
}
