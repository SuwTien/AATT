PS J:\DEV\Android\AATT> ./gradlew installDebug

> Task :app:compileDebugKotlin FAILED
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:393:47 Overload resolution ambiguity between candidates:
@IntrinsicConstEvaluation() fun plus(other: Byte): Long
@IntrinsicConstEvaluation() fun plus(other: Double): Double
@IntrinsicConstEvaluation() fun plus(other: Float): Float
@IntrinsicConstEvaluation() fun plus(other: Int): Long
@IntrinsicConstEvaluation() fun plus(other: Long): Long
@IntrinsicConstEvaluation() fun plus(other: Short): Long
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:393:49 Unresolved reference 'totalDeplacementDuration'.
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:393:74 Overload resolution ambiguity between candidates:
fun String?.plus(other: Any?): String
fun <T> Array<T>.plus(element: T): Array<T>
fun LongArray.plus(element: Long): LongArray
fun <T> Collection<T>.plus(element: T): List<T>
fun <T> Iterable<T>.plus(element: T): List<T>
fun <T> Set<T>.plus(element: T): Set<T>
fun <T> Sequence<T>.plus(element: T): Sequence<T>
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:394:50 Overload resolution ambiguity between candidates:
fun String?.plus(other: Any?): String
fun <T> Array<T>.plus(element: T): Array<T>
fun LongArray.plus(element: Long): LongArray
fun <T> Collection<T>.plus(element: T): List<T>
fun <T> Iterable<T>.plus(element: T): List<T>
fun <T> Set<T>.plus(element: T): Set<T>
fun <T> Sequence<T>.plus(element: T): Sequence<T>
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:426:9 Unresolved reference 'activitiesToDisplay'.
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:426:39 Cannot 
infer type for this parameter. Please specify it explicitly.
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:426:39 Function 'component1()' is ambiguous for this expression:
@InlineOnly() fun <T> Array<out T>.component1(): T
@InlineOnly() fun BooleanArray.component1(): Boolean
@InlineOnly() fun ByteArray.component1(): Byte
@InlineOnly() fun CharArray.component1(): Char
@InlineOnly() fun DoubleArray.component1(): Double
@InlineOnly() fun FloatArray.component1(): Float
@InlineOnly() fun IntArray.component1(): Int
@InlineOnly() fun LongArray.component1(): Long
@InlineOnly() fun ShortArray.component1(): Short
@InlineOnly() fun <T> List<T>.component1(): T
@InlineOnly() fun <K, V> Map.Entry<K, V>.component1(): K
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UByteArray.component1(): UByte
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UIntArray.component1(): UInt
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun ULongArray.component1(): ULong
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UShortArray.component1(): UShort.
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:426:39 Function 'component2()' is ambiguous for this expression:
@InlineOnly() fun <T> Array<out T>.component2(): T
@InlineOnly() fun BooleanArray.component2(): Boolean
@InlineOnly() fun ByteArray.component2(): Byte
@InlineOnly() fun CharArray.component2(): Char
@InlineOnly() fun DoubleArray.component2(): Double
@InlineOnly() fun FloatArray.component2(): Float
@InlineOnly() fun IntArray.component2(): Int
@InlineOnly() fun LongArray.component2(): Long
@InlineOnly() fun ShortArray.component2(): Short
@InlineOnly() fun <T> List<T>.component2(): T
@InlineOnly() fun <K, V> Map.Entry<K, V>.component2(): V
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UByteArray.component2(): UByte
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UIntArray.component2(): UInt
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun ULongArray.component2(): ULong
@SinceKotlin(...) @ExperimentalUnsignedTypes() @InlineOnly() fun UShortArray.component2(): UShort.
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:427:13 @Composable invocations can only happen from the context of a @Composable function
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:433:17 Overload resolution ambiguity between candidates:
@Composable() @ComposableTarget(...) fun Text(text: AnnotatedString, modifier: Modifier = ..., color: Color 
= ..., fontSize: TextUnit = ..., fontStyle: FontStyle? = ..., fontWeight: FontWeight? = ..., fontFamily: FontFamily? = ..., letterSpacing: TextUnit = ..., textDecoration: TextDecoration? = ..., textAlign: TextAlign? 
= ..., lineHeight: TextUnit = ..., overflow: TextOverflow = ..., softWrap: Boolean = ..., maxLines: Int = ..., minLines: Int = ..., inlineContent: Map<String, InlineTextContent> = ..., onTextLayout: (TextLayoutResult) -> Unit = ..., style: TextStyle = ...): Unit
@Composable() @ComposableTarget(...) fun Text(text: String, modifier: Modifier = ..., color: Color = ..., fontSize: TextUnit = ..., fontStyle: FontStyle? = ..., fontWeight: FontWeight? = ..., fontFamily: FontFamily? 
= ..., letterSpacing: TextUnit = ..., textDecoration: TextDecoration? = ..., textAlign: TextAlign? = ..., lineHeight: TextUnit = ..., overflow: TextOverflow = ..., softWrap: Boolean = ..., maxLines: Int = ..., minLines: Int = ..., onTextLayout: ((TextLayoutResult) -> Unit)? = ..., style: TextStyle = ...): Unit
e: file:///J:/DEV/Android/AATT/app/src/main/java/fr/bdst/aatt/ui/screens/WeeklyStatsScreen.kt:437:17 Overload resolution ambiguity between candidates:
@Composable() @ComposableTarget(...) fun Text(text: AnnotatedString, modifier: Modifier = ..., color: Color 
= ..., fontSize: TextUnit = ..., fontStyle: FontStyle? = ..., fontWeight: FontWeight? = ..., fontFamily: FontFamily? = ..., letterSpacing: TextUnit = ..., textDecoration: TextDecoration? = ..., textAlign: TextAlign? 
= ..., lineHeight: TextUnit = ..., overflow: TextOverflow = ..., softWrap: Boolean = ..., maxLines: Int = ..., minLines: Int = ..., inlineContent: Map<String, InlineTextContent> = ..., onTextLayout: (TextLayoutResult) -> Unit = ..., style: TextStyle = ...): Unit
@Composable() @ComposableTarget(...) fun Text(text: String, modifier: Modifier = ..., color: Color = ..., fontSize: TextUnit = ..., fontStyle: FontStyle? = ..., fontWeight: FontWeight? = ..., fontFamily: FontFamily? 
= ..., letterSpacing: TextUnit = ..., textDecoration: TextDecoration? = ..., textAlign: TextAlign? = ..., lineHeight: TextUnit = ..., overflow: TextOverflow = ..., softWrap: Boolean = ..., maxLines: Int = ..., minLines: Int = ..., onTextLayout: ((TextLayoutResult) -> Unit)? = ..., style: TextStyle = ...): Unit

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.10.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD FAILED in 1s
30 actionable tasks: 1 executed, 29 up-to-date
PS J:\DEV\Android\AATT> 