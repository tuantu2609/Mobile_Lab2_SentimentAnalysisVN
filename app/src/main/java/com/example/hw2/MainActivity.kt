package com.example.hw2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hw2.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    // Thay đổi URL API thành địa chỉ Flask API của bạn
    private val apiBaseUrl = "http://10.0.2.2:5000" // Thay bằng IP máy tính chạy Flask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SentimentAnalyzer(apiBaseUrl)
        }
    }
}

@Composable
fun SentimentAnalyzer(apiBaseUrl: String) {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var resultText by remember { mutableStateOf("Nhập văn bản để phân tích") }
    var sentiment by remember { mutableStateOf("neutral") } // neutral/positive/negative
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Cập nhật phần ánh xạ sentiment
    val (backgroundColor, emoji) = when (sentiment.lowercase()) {
        "pos" -> Pair(Color(0xFF4CAF50), R.drawable.smile) // Xanh lá - POS
        "neg" -> Pair(Color(0xFFB71C1C), R.drawable.sad) // Đỏ - NEG
        else -> Pair(Color(0xFFEEEEEE), R.drawable.neutral) // Xám - Neutral
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = userInput,
            onValueChange = { newValue ->
                userInput = newValue
            },
            label = { Text("Nhập câu cần phân tích cảm xúc") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                autoCorrect = true,
                keyboardType = KeyboardType.Text
            ),
            visualTransformation = VisualTransformation.None
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (userInput.text.isNotBlank()) {
                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val analysisResult = analyzeSentiment(apiBaseUrl, userInput.text)
                            withContext(Dispatchers.Main) {
                                // Giữ nguyên giá trị trả về từ API (POS/NEG)
                                sentiment = analysisResult.lowercase()

                                // Chỉ cập nhật text hiển thị
                                resultText = when (sentiment) {
                                    "pos" -> "Tích cực"
                                    "neg" -> "Tiêu cực"
                                    else -> "Trung tính"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                resultText = "Lỗi: ${e.message}"
                                sentiment = "neutral"
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                } else {
                    resultText = "Vui lòng nhập văn bản!"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Phân tích cảm xúc")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = emoji),
            contentDescription = "Biểu tượng cảm xúc",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = resultText, style = MaterialTheme.typography.bodyLarge)
    }
}

suspend fun analyzeSentiment(apiBaseUrl: String, text: String): String {
    return try {
        val response = RetrofitInstance(apiBaseUrl).api.getSentiment(
            PhoBERTRequest(text = text)
        )

        if (!response.isSuccessful) {
            return "Lỗi API: ${response.code()}"
        }

        response.body()?.sentiment ?: "Không thể phân tích"
    } catch (e: Exception) {
        "Lỗi kết nối: ${e.message}"
    }
}