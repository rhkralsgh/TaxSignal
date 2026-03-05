package project.taxsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    Column(modifier = Modifier.padding(16.dp)) {
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
            SalaryResultCard(result)
        }
    }
}

@Composable
fun SalaryResultCard(result: SalaryResult) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ResultRow("예상 실수령액", "${result.actualPay}원", isBold = true, isHighlight = true)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "상세 공제 내역",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            ResultRow("국민연금", "-${result.nationalPension}원")
            ResultRow("건강보험", "-${result.healthInsurance}원")
            ResultRow("장기요양", "-${result.longTermCare}원")
            ResultRow("고용보험", "-${result.employmentInsurance}원")
            ResultRow("소득세", "-${result.incomeTax}원")
            ResultRow("지방소득세", "-${result.localIncomeTax}원")
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

}