package com.example.unitconvertor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnitConverterApp()
        }
    }
}


@Composable
fun UnitConverterApp() {
    var inputValue by remember { mutableStateOf("") } // Stores user input value
    var selectedCategory by remember { mutableStateOf("Length") } // Tracks selected measurement category
    var sourceUnit by remember { mutableStateOf("Inch") } // Tracks source unit for conversion
    var destinationUnit by remember { mutableStateOf("Centimeter") } // Tracks destination unit for conversion
    var convertedValue by remember { mutableStateOf("Result") } // Stores converted value
    var errorMessage by remember { mutableStateOf("") } // Stores error message if input is invalid

    val categories = listOf("Length", "Weight", "Temperature") // List of measurement categories
    val lengthUnits = listOf("Inch", "Foot", "Yard", "Mile", "Centimeter", "Meter", "Kilometer") // Units for length
    val weightUnits = listOf("Pound", "Ounce", "Ton", "Kilogram", "Gram") // Units for weight
    val tempUnits = listOf("Celsius", "Fahrenheit", "Kelvin") // Units for temperature


    // When the category is changed, reset source and destination units
    val units = when (selectedCategory) {
        "Length" -> lengthUnits
        "Weight" -> weightUnits
        "Temperature" -> tempUnits
        else -> lengthUnits
    }

    // Set the default source and destination units based on category
    val defaultSourceUnit = when (selectedCategory) {
        "Length" -> "Inch"
        "Weight" -> "Pound"
        "Temperature" -> "Celsius"
        else -> "Inch"
    }
    val defaultDestinationUnit = when (selectedCategory) {
        "Length" -> "Centimeter"
        "Weight" -> "Kilogram"
        "Temperature" -> "Fahrenheit"
        else -> "Centimeter"
    }

    // Reset source and destination units when category changes
    LaunchedEffect(selectedCategory) {
        sourceUnit = defaultSourceUnit
        destinationUnit = defaultDestinationUnit
    }

    Column(modifier = Modifier.padding(16.dp)) { // UI Layout with padding
        Text("Unit Converter", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp)) // Adds spacing between components

        // Dropdown for selecting measurement category
        DropdownMenuSelector("Category", categories, selectedCategory) { selectedCategory = it }
        DropdownMenuSelector("From", units, sourceUnit) { sourceUnit = it } // Dropdown for source unit
        DropdownMenuSelector("To", units, destinationUnit) { destinationUnit = it } // Dropdown for destination unit

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Restricts input to numbers
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ElevatedButton(
            onClick = {
                try {
                    val value = inputValue.toDouble()
                    // Check if the source and destination units are the same
                    if (sourceUnit == destinationUnit) {
                        convertedValue = "$value $destinationUnit"
                        errorMessage = "Value is same because of the same unit conversion "
                    }else{
                        convertedValue = performConversion(value, selectedCategory, sourceUnit, destinationUnit)
                        errorMessage = ""
                    }

                } catch (e: Exception) {
                    errorMessage = "Invalid input, please enter a valid number"
                    convertedValue = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        )  {
            Text("Convert")
        }

        if (errorMessage.isNotEmpty()) { // Displays error message if input is invalid
            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (convertedValue.isNotEmpty()) { // Displays converted value if available
            OutlinedCard(modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                Text(convertedValue, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuSelector(label: String, options: List<String>, selected: String, onSelectionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) } // Tracks dropdown expansion state
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option) // Updates selected option when clicked
                        expanded = false // Closes dropdown menu
                    }
                )
            }
        }
    }
}


fun performConversion(value: Double, category: String, from: String, to: String): String {
    val convertedValue = when (category) {
        "Length" -> convertLength(value, from, to)
        "Weight" -> convertWeight(value, from, to)
        "Temperature" -> convertTemperature(value, from, to)
        else -> "Conversion not supported"
    }
    return String.format("%.6f %s", convertedValue, to)
}

// Length Conversion Function
fun convertLength(value: Double, from: String, to: String): Double {
    val lengthMap = mapOf(
        "Inch" to 2.54, "Foot" to 30.48, "Yard" to 91.44, "Mile" to 160934.0,
        "Centimeter" to 1.0, "Meter" to 100.0, "Kilometer" to 100000.0
    )

    return if (from in lengthMap && to in lengthMap) {
        (value * lengthMap[from]!!) / lengthMap[to]!!
    } else value
}

// Weight Conversion Function
fun convertWeight(value: Double, from: String, to: String): Double {
    val weightMap = mapOf(
        "Pound" to 453.592, "Ounce" to 28.3495, "Ton" to 907184.0, "Kilogram" to 1000.0, "Gram" to 1.0
    )

    return if (from in weightMap && to in weightMap) {
        (value * weightMap[from]!!) / weightMap[to]!!
    } else value
}

// Temperature Conversion Function
fun convertTemperature(value: Double, from: String, to: String): Double {
    return when {
        from == "Celsius" && to == "Fahrenheit" -> (value * 1.8) + 32
        from == "Fahrenheit" && to == "Celsius" -> (value - 32) / 1.8
        from == "Celsius" && to == "Kelvin" -> value + 273.15
        from == "Kelvin" && to == "Celsius" -> value - 273.15
        from == "Fahrenheit" && to == "Kelvin" -> ((value - 32) / 1.8) + 273.15
        from == "Kelvin" && to == "Fahrenheit" -> ((value - 273.15) * 1.8) + 32
        else -> value
    }
}