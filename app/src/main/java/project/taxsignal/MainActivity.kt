package project.taxsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import project.taxsignal.model.SalaryResult
import project.taxsignal.ui.theme.TaxSignalTheme
import project.taxsignal.viewmodel.SalaryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SalaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxSignalTheme {
                MainScreen(viewModel)
            }
        }
    }
}

//화면 정보
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Salary: Screen("salary", "급여 계산", Icons.Default.Calculate)
    object TrafficLight: Screen("traffic_light", "신호등", Icons.Default.LightMode)
    object Simulate: Screen("simulate", "절세 팁", Icons.Default.Savings)
}
@Composable
fun MainScreen(viewModel: SalaryViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Salary, Screen.TrafficLight, Screen.Simulate)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {Icon(screen.icon, contentDescription = null) },
                        label = {Text(screen.title)},
                        //네비게이션 화면은 트리구조이기때문에 하위 화면을 대비하여 hierarachy 사용
                        selected = currentDestination?.hierarchy?.any {it.route == screen.route} == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // 텍스트 창 월급 입력 값 저장
                                }
                                launchSingleTop = true
                                restoreState = true // 저장된 값 불러오기
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Salary.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Salary.route) { SalaryScreen(viewModel) }
            composable(Screen.TrafficLight.route) { TrafficLightScreen(viewModel) }
            composable(Screen.Simulate.route) { SimulatorScreen(viewModel) }
        }
    }
}

@Composable
fun SalaryScreen(viewModel: SalaryViewModel) {
    val inputSalary by viewModel.inputSalary.collectAsState()
    val salaryResult by viewModel.salaryResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.padding(16.dp).verticalScroll(scrollState)) {
        Text(text = "내 월급 입력", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = inputSalary,
            // 숫자만 적용 filter
            onValueChange = { newValue -> viewModel.onSalaryChanged(newValue.filter { char -> char.isDigit() }) },
            label = { Text("세전 월급 (원)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        salaryResult?.let { result ->
            SalaryResultCard(result, viewModel)
        }
    }
}

@Composable
fun SalaryResultCard(result: SalaryResult, viewModel: SalaryViewModel) {
    val extraItems by viewModel.additionalDeductions.collectAsState()
    val totalExtra by viewModel.totalAdditionalAmount.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("연금저축") } //선택한 옵션
    var customName by remember { mutableStateOf("") } // 입력한 이름
    var amountInput by remember { mutableStateOf("") } // 입력한 금액
    val options = listOf("연금저축", "월세", "직접 입력")

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ResultRow(
                "최종 가용 현금",
                "${result.actualPay - totalExtra}원",
                isBold = true,
                isHighlight = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "상세 공제 내역",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 법정 공제 내역
            ResultRow("국민연금", "-${result.nationalPension}원")
            ResultRow("건강보험", "-${result.healthInsurance}원")
            ResultRow("장기요양", "-${result.longTermCare}원")
            ResultRow("고용보험", "-${result.employmentInsurance}원")
            ResultRow("소득세", "-${result.incomeTax}원")
            ResultRow("지방소득세", "-${result.localIncomeTax}원")

            // 사용자 추가 항목 리스트
            extraItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ㄴ ${item.name}", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "-${item.amount}원", style = MaterialTheme.typography.bodySmall)
                        IconButton(
                            onClick = { viewModel.removeDeduction(item.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if(!isAdding) {
                OutlinedButton(
                    onClick = {isAdding = true},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("공제 항목 추가")
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    //항목 선택
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        options.forEach {option ->
                            FilterChip(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option },
                                label = {Text(option)},
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                    //직접 입력 이름 칸
                    if(selectedOption == "직접 입력") {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("항목 이름") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                        )
                    }
                    // 금액 입력 칸
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = {amountInput = it.filter { c -> c.isDigit() } },
                        label = {Text("월 금액")},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAdding = false }) {Text("취소")}
                        TextButton(onClick = {
                            val finalName = if (selectedOption == "직접 입력") customName else selectedOption
                            if (finalName.isNotEmpty() && amountInput.isNotEmpty()) {
                                viewModel.addDeduction(finalName, amountInput.toLong())
                                isAdding = false
                                amountInput = ""
                                customName = ""
                            }
                        },
                            enabled = amountInput.isNotEmpty() && (selectedOption != "직접 입력" || customName.isNotEmpty())
                        ) {Text("확인")}
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isBold: Boolean = false, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun TrafficLightScreen(viewModel: SalaryViewModel) {
    // 월간 데이터 구독
    val monthlyCardStr by viewModel.monthlyCardSpending.collectAsState()
    val monthlyThreshold by viewModel.monthlyThreshold.collectAsState()

    val currentSpending = monthlyCardStr.toLongOrNull() ?: 0L

    // 월간 진행률 계산
    val progress = if (monthlyThreshold > 0) {
        (currentSpending.toFloat() / monthlyThreshold).coerceIn(0f, 1f)
    } else 0f

    // 월간 기준 신호등 로직
    val (statusColor, statusMsg) = when {
        monthlyThreshold == 0L -> MaterialTheme.colorScheme.outline to "급여를 먼저 입력해주세요"
        currentSpending < monthlyThreshold -> Color(0xFFFFC107) to "목표까지 ${monthlyThreshold - currentSpending}원 남았습니다!"
        else -> Color.Green to "이번 달 목표 달성!"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "이달의 소득공제 신호등", style = MaterialTheme.typography.headlineMedium)
        Text(text = "연봉 25% 달성을 위한 월간 권장 소비량입니다.", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = statusMsg, fontWeight = FontWeight.Bold, color = statusColor)
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "이번 달: ${currentSpending}원", style = MaterialTheme.typography.bodySmall)
                    Text(text = "월 목표: ${monthlyThreshold}원", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = monthlyCardStr,
            onValueChange = { viewModel.onMonthlyCardChanged(it.filter { c -> c.isDigit() }) },
            label = { Text("이번 달 카드 사용액") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SimulatorScreen(viewModel: SalaryViewModel) {
    val annualPensionSaving by viewModel.annualPensionSaving.collectAsState()
    val inputSalary by viewModel.inputSalary.collectAsState()

    //연봉 계산
    val annualSalary = (inputSalary.toLongOrNull() ?: 0L) * 12

    //연봉 5,500만원 기준 공제율 설정(16.5% or 13.2%)
    val taxRate = if(annualSalary > 0 && annualSalary <= 55_000_000L) 0.165 else 0.132
    val ratePercentage = if (taxRate == 0.165) "16.5%" else "13.2%"
    //예상 환금액
    val expectedRefund = (annualPensionSaving * taxRate).toLong()

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(text = "💰 절세 시뮬레이터", style = MaterialTheme.typography.headlineMedium)
        Text(text = "저축만으로 얻는 확실한 수익을 확인하세요.", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "연금저축 / IRP 혜택", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 현재 상태 정보
                DetailInfoRow("나의 연간 저축액", "${annualPensionSaving}원")
                DetailInfoRow("적용 공제율 (지방세 포함)", ratePercentage)

                Spacer(modifier = Modifier.height(16.dp))

                // 결과 표시 박스
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "내년 초 예상 환급액", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${expectedRefund}원",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "* 연간 총 급여 ${annualSalary / 10000}만원 기준 공제율이 적용되었습니다.\n* 실제 환급액은 결정세액에 따라 달라질 수 있습니다.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 시뮬레이터 전용 소형 정보 로우
@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}