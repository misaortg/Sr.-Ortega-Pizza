package com.example.srortegapizza

// --- IMPORTACIONES DEL SISTEMA ---
// Se importan los componentes necesarios para el desarrollo de esta app,
// la estructura de layouts (Column, Row, Box), listas optimizadas (LazyColumn),
// el motor de estados de Compose (runtime) y las herramientas de diseño de Material 3.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- ACTIVIDAD PRINCIPAL ---
// Punto de entrada de la aplicación en Android. Hereda de ComponentActivity.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent define que la interfaz gráfica se manejará mediante Jetpack Compose
        setContent {
            // MaterialTheme aplica los estilos, tipografías y colores base de Material Design 3
            MaterialTheme {
                MainScreen() // Invoca al contenedor principal de la aplicación
            }
        }
    }
}

// --- MODELO DE DATOS (ESTRUCTURA INMUTABLE) ---
// Molde  para estructurar los datos de cada producto de forma unificada.
data class PizzaItem(
    val nombre: String,       // Nombre comercial de la pizza o combo
    val precio: String,       // Costo formateado con divisa (ej: $400)
    val descripcion: String,  // Breve reseña comercial para la lista
    val ingredientes: String // Detalle profundo exclusivo para la pantalla de detalle
)

// --- PANTALLA PRINCIPAL: CONTROLADOR DE NAVEGACIÓN Y ESTADOS ---
// Esta función actúa como el "cerebro" de la app. Gestiona el enrutamiento usando State Hoisting.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // ESTADO 1: Controla qué pantalla está activa en la interfaz ("inicio", "menu", "ofertas", "detalle")
    var pantallaActual by remember { mutableStateOf("inicio") }

    // ESTADO 2: Almacena el objeto PizzaItem seleccionado cuando el usuario desea ver un detalle
    var pizzaSeleccionada by remember { mutableStateOf<PizzaItem?>(null) }

    // CONTEXTO: Se obtiene el contexto de la app para poder interactuar con funciones del sistema operativo (como cerrar la app)
    val context = LocalContext.current

    // Scaffold provee la estructura visual estándar de Android (Barra superior y Barra de navegación inferior)
    Scaffold(
        // CONFIGURACIÓN DE LA BARRA SUPERIOR (TopAppBar)
        topBar = {
            TopAppBar(
                title = { Text("Sr. Ortega Pizza", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB71C1C)), // Rojo gourmet
                actions = {
                    // REQUERIMIENTO: Botón para cerrar la aplicación de manera definitiva
                    TextButton(onClick = {
                        // Transforma el contexto de forma segura en la actividad actual y llama a finish() para destruir la app
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Text("❌ Cerrar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        // CONFIGURACIÓN DE LA BARRA INFERIOR (NavigationBar)
        bottomBar = {
            // Condición: La barra de navegación sólo se muestra en las pantallas principales. Se oculta en el "detalle".
            if (pantallaActual in listOf("inicio", "menu", "ofertas")) {
                NavigationBar(containerColor = Color(0xFFFAFAFA)) {
                    // Opción 1: Pestaña Inicio
                    NavigationBarItem(
                        selected = pantallaActual == "inicio",
                        onClick = { pantallaActual = "inicio" },
                        label = { Text("Inicio") },
                        icon = { Text("🏠", fontSize = 20.sp) }
                    )
                    // Opción 2: Pestaña Menú Principal
                    NavigationBarItem(
                        selected = pantallaActual == "menu",
                        onClick = { pantallaActual = "menu" },
                        label = { Text("Menú") },
                        icon = { Text("🍕", fontSize = 20.sp) }
                    )
                    // Opción 3: Pestaña Ofertas
                    NavigationBarItem(
                        selected = pantallaActual == "ofertas",
                        onClick = { pantallaActual = "ofertas" },
                        label = { Text("Ofertas") },
                        icon = { Text("🔥", fontSize = 20.sp) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Box contenedor que aplica los márgenes automáticos generados por el Scaffold (paddingValues)
        Box(modifier = Modifier.padding(paddingValues)) {
            // ENRUTADOR DINÁMICO: Cambia el contenido según el valor de la variable 'pantallaActual'
            when (pantallaActual) {
                // Si el estado es "inicio", dibuja la Pantalla de Inicio
                "inicio" -> PantallaInicio(onVerMenuClick = { pantallaActual = "menu" })

                // Si el estado es "menu", dibuja el catálogo principal
                "menu" -> PantallaMenu(onPizzaClick = { pizza ->
                    pizzaSeleccionada = pizza       // Guarda la información de la pizza seleccionada
                    pantallaActual = "detalle"     // Salta a la pantalla de detalle
                })

                // Si el estado es "ofertas", dibuja la sección de promociones
                "ofertas" -> PantallaOfertas(onPizzaClick = { pizza ->
                    pizzaSeleccionada = pizza       // Guarda la información de la oferta seleccionada
                    pantallaActual = "detalle"     // Salta a la pantalla de detalle
                })

                // Si el estado es "detalle", desempaqueta de forma segura la pizza guardada y la muestra
                "detalle" -> {
                    pizzaSeleccionada?.let { pizza ->
                        PantallaDetalle(
                            pizza = pizza,
                            onVolverClick = { pantallaActual = "menu" } // REQUERIMIENTO: Botón para regresar al menú principal
                        )
                    }
                }
            }
        }
    }
}

// --- 1. COMPOSABLE: PANTALLA DE INICIO ---
@Composable
fun PantallaInicio(onVerMenuClick: () -> Unit) {
    // Column organiza los elementos verticalmente, centrandolo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,     // REQUERIMIENTO: Diseño visual centrado verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // REQUERIMIENTO: Diseño visual centrado horizontalmente
    ) {
        // REQUERIMIENTO: Uso de elemento de imagen/visual destacado compatible con Previews sin recursos externos
        Text(
            text = "🍕",
            fontSize = 90.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp)) // REQUERIMIENTO: Uso correcto de Spacer para espaciados controlados

        Text(
            text = "¡BIENVENIDO!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Las mejores pizzas artesanales a la leña con el toque secreto de la casa.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // REQUERIMIENTO: Botón de acceso al menú principal
        Button(
            onClick = onVerMenuClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)), // Naranja premium
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Ver el Menú", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// --- 2. COMPOSABLE: PANTALLA DE MENÚ PRINCIPAL ---
@Composable
fun PantallaMenu(onPizzaClick: (PizzaItem) -> Unit) {
    // REQUERIMIENTO: Menú principal con múltiples opciones de productos
    val pizzas = listOf(
        PizzaItem("Pizza de Jamon", "$450", "Jamon Ibérico, mozzarella fresca y albahaca.", "Jamon Ibérico, Queso Mozzarella de Búfala, Albahaca fresca, Aceite de oliva extra virgen."),
        PizzaItem("Pizza Pepperoni", "$400", "El clásico americano con abundante pepperoni crujiente.", "Salsa de la casa, Queso Mozzarella, Pepperoni madurado artesanal, Orégano."),
        PizzaItem("Pizza Cuatro Quesos", "$600", "Mezcla perfecta de quesos seleccionados.", "Queso Mozzarella, Gorgonzola, Parmesano Reggiano, Provolone ahumado."),
        PizzaItem("Pizza BBQ Chicken", "$550", "Pollo desmenuzado y nuestra salsa BBQ especial.", "Pechuga de pollo a la parilla, Salsa BBQ ahumada, Cebolla morada, Mozzarella.")
    )

    // LazyColumn es un contenedor optimizado que sólo dibuja los elementos que se ven en pantalla (Scrolleable)
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Aplica una separación fija de 16.dp entre cada tarjeta
    ) {
        // Cabecera de la lista
        item {
            Text("Nuestro Menú", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(4.dp))
        }
        // Renderizado dinámico del listado de pizzas
        items(pizzas) { pizza ->
            // Reutiliza el componente TarjetaProducto pasándole los datos individuales y la acción de clic
            TarjetaProducto(pizza = pizza, botonTexto = "Ver Detalle", onClick = { onPizzaClick(pizza) })
        }
    }
}

// --- 3. COMPOSABLE: PANTALLA DE OFERTAS ---
@Composable
fun PantallaOfertas(onPizzaClick: (PizzaItem) -> Unit) {
    // Estructura de datos local para almacenar los combos y promociones de la pizzería
    val ofertas = listOf(
        PizzaItem("Combo Familiar", "$1000", "2 Pizzas medianas + Refresco de 2L.", "2 Pizzas medianas a elección (Margherita o Pepperoni) junto a una bebida familiar de 2 Litros."),
        PizzaItem("Paquete Futbolero", "$800", "1 Pizza grande + Alitas de pollo ($300).", "1 Pizza grande clásica combinada con 8 deliciosas alitas de pollo bañadas en salsa BBQ.")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Ofertas Especiales 🔥", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(4.dp))
        }
        items(ofertas) { oferta ->
            // Reutiliza TarjetaProducto pero altera su color de fondo a un tono crema (0xFFFFF8E1) para diferenciarlo
            TarjetaProducto(
                pizza = oferta,
                botonTexto = "Ver Detalle",
                colorFondo = Color(0xFFFFF8E1),
                onClick = { onPizzaClick(oferta) }
            )
        }
    }
}

// --- 4. COMPOSABLE: PANTALLA DE DETALLE ---
@Composable
fun PantallaDetalle(pizza: PizzaItem, onVolverClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Contenedor circular estético superior para el icono decorativo
        Card(
            shape = RoundedCornerShape(50.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("📋", fontSize = 50.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Muestra de manera organizada el título del producto y su precio destacado
        Text(text = pizza.nombre, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = pizza.precio, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))

        Spacer(modifier = Modifier.height(24.dp))

        // Bloque informativo gris para desglosar la descripción y los ingredientes completos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Descripción:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                Text(pizza.descripcion, fontSize = 16.sp, color = Color(0xFF424242))

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ingredientes Incluidos:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                Text(pizza.ingredientes, fontSize = 16.sp, color = Color(0xFF424242))
            }
        }

        // TRUCO DE COMPOSE: El peso weight(1f) fuerza a que el espacio en blanco se estire al máximo,
        // empujando el botón inferior de regreso al fondo exacto de la pantalla.
        Spacer(modifier = Modifier.weight(1f))

        // REQUERIMIENTO: Botón explícito para regresar al menú principal
        Button(
            onClick = onVolverClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)), // Gris corporativo oscuro
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("⬅  Regresar al Menú", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 5. COMPONENTE REUTILIZABLE: TARJETA DE PRODUCTO ---
// Abstracción visual para maquetar los productos reduciendo la redundancia de código.
@Composable
fun TarjetaProducto(pizza: PizzaItem, botonTexto: String, colorFondo: Color = Color(0xFFF5F5F5), onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila horizontal que distribuye el título a la izquierda y el precio a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // weight(1f) evita que nombres largos pisen o desplacen el texto del precio
                Text(pizza.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Color(0xFF212121))
                Text(pizza.precio, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            }

            Text(pizza.descripcion, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

            // Botón de acción alineado al extremo inferior derecho de la tarjeta
            Button(
                onClick = onClick, // Ejecuta el callback delegado desde la jerarquía superior
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.align(Alignment.End).height(38.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(botonTexto, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ========================================================================
// ---IMPLEMENTACIÓN FUNCIONAL DE ENTORNO DE PREVIEWS ---
// ========================================================================

// PREVIEW 1: Muestra toda la app simulada desde su flujo inicial
@Preview(name = "Inicio", showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaPrincipal() {
    MaterialTheme {
        MainScreen()
    }
}

// PREVIEW 2: Entorno aislado para inspeccionar los estilos de la lista de productos
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Menú", showBackground = true, showSystemUi = true)
@Composable
fun PreviewSoloMenu() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Menú - Vista Previa", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB71C1C))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PantallaMenu(onPizzaClick = {})
            }
        }
    }
}

// PREVIEW 3: Entorno aislado para inspeccionar el listado de las ofertas
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Ofertas Especiales", showBackground = true, showSystemUi = true)
@Composable
fun PreviewSoloOfertas() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ofertas - Vista Previa", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB71C1C))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PantallaOfertas(onPizzaClick = {})
            }
        }
    }
}

// PREVIEW 4: Inyecta un objeto simulado a la vista de detalle para validar márgenes y legibilidad de textos en caliente
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Detalle Independiente", showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaDetalle() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle de Producto", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB71C1C))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PantallaDetalle(
                    pizza = PizzaItem(
                        nombre = "Pizza Cuatro Quesos",
                        precio = "$600",
                        descripcion = "Mezcla perfecta de quesos seleccionados sobre base crujiente.",
                        ingredientes = "Queso Mozzarella, Gorgonzola, Parmesano Reggiano, Provolone ahumado, Albahaca."
                    ),
                    onVolverClick = {}
                )
            }
        }
    }
}