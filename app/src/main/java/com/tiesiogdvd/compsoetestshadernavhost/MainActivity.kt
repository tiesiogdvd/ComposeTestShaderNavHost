package com.tiesiogdvd.compsoetestshadernavhost

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            Surface(
                modifier = Modifier
                    .basicTimeShader(true)
                    .fillMaxSize()
            ){}
            Navigation(navController, "test")
        }
    }
}

@Composable
fun Navigation(navController: NavHostController, route: String) {
    NavHost(navController = navController, startDestination = "test") {
        composable(
            "test"
        )
        {

        }
    }
}

const val FRACTAL_SHADER_SRC = """
    uniform float2 size;
    uniform float time;
    uniform shader composable;
    
    float f(float3 p) {
        p.z -= time * 2.;
        float a = p.z * .1;
        p.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        return .1 - length(cos(p.xy) + sin(p.yz));
    }
    
    half4 main(float2 fragcoord) { 
        float3 d = .5 - fragcoord.xy1 / size.y;
        float3 p=float3(0);
        for (int i = 0; i < 32; i++) {
          p += f(p) * d;
        }
        return ((sin(p) + float3(2, 5, 12)) / length(p)).xyz1;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.basicTimeShader(
    isEnabled: Boolean,
) = composed {
    val shader = remember(Unit) { RuntimeShader(FRACTAL_SHADER_SRC) }
    var time by remember { mutableStateOf(0f) }
    var lastTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var composableSize by remember {
        mutableStateOf(
            Size(
                0f,
                0f
            )
        )
    }

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            while (isActive) {
                val currentTimeMillis = System.currentTimeMillis()
                val deltaTime = (currentTimeMillis - lastTimeMillis) / 1000f
                time += deltaTime
                lastTimeMillis = currentTimeMillis
                delay(16L)
            }
        }
    }
    this
        .onSizeChanged { size ->
            composableSize = Size(size.width.toFloat(), size.height.toFloat())
            shader.setFloatUniform("size", size.width.toFloat(), size.height.toFloat())
        }

        .graphicsLayer {
            shader.setFloatUniform("time", time)
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "composable")
                .asComposeRenderEffect()
        }
}