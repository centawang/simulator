package com.simulator.adder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdderScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AdderScreen(modifier: Modifier = Modifier) {
    var firstNumber by remember { mutableStateOf("") }
    var secondNumber by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var firstError by remember { mutableStateOf(false) }
    var secondError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Integer Adder",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = firstNumber,
                onValueChange = {
                    firstNumber = it
                    firstError = false
                },
                label = { Text("First integer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = firstError,
                supportingText = if (firstError) {
                    { Text("Enter a valid integer") }
                } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = secondNumber,
                onValueChange = {
                    secondNumber = it
                    secondError = false
                },
                label = { Text("Second integer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = secondError,
                supportingText = if (secondError) {
                    { Text("Enter a valid integer") }
                } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val a = firstNumber.trim().toLongOrNull()
            val b = secondNumber.trim().toLongOrNull()
            firstError = a == null
            secondError = b == null
            if (a != null && b != null) {
                result = "${a + b}"
            }
        }) {
            Text("Add")
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Result: $it",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}
