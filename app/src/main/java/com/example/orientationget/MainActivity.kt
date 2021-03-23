package com.example.orientationget

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orientationget.databinding.ActivityMainBinding
import java.lang.StringBuilder

class MainActivity : AppCompatActivity(),SensorEventListener {
    lateinit var binding:ActivityMainBinding

    private val matrixSize = 16
    //センサーの値
    private var mgValues = FloatArray(3)
    private var acValues = FloatArray(3)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * センサーの値が更新されたときに呼び出される
     * @param event
     */
    override fun onSensorChanged(event: SensorEvent?) {
        //inRとoutR,Iは回転行列で16個の要素を持つ配列
        val inR = FloatArray(matrixSize)
        val outR = FloatArray(matrixSize)
        val I = FloatArray(matrixSize)
        val orValues = FloatArray(3)

        if(event == null)return
        when(event.sensor.type){
            //eventの値によって分岐させる。値は各配列にclone()メソッドで複製する
            Sensor.TYPE_ACCELEROMETER -> acValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> mgValues = event.values.clone()
        }
        //加速度センサーと地磁気センサーの値から回転行列inR,Iを作成する。
        SensorManager.getRotationMatrix(inR,I,acValues,mgValues)

        //inRを異なる座標系軸へ行列変換してoutRに出力する。第2,3引数はAndroid端末のX軸、Y軸が指す世界座標系の方向を指定する。
        //携帯を水平に持ち、アクティビティはポートレイト(縦長表示)
        //様々な置き方に対応できる
        //SensorManager.remapCoordinateSystem(inR,SensorManager.AXIS_X,SensorManager.AXIS_Y,outR)
        SensorManager.remapCoordinateSystem(inR,SensorManager.AXIS_X,SensorManager.AXIS_Z,outR)

        //求めた回転行列outR引数に指定して、方位角(アジマス),傾斜角(ピッチ),回転角(ロール)を配列として取得する。
        //このメソッドAndroid端末の姿勢の変化に対応できる点
        SensorManager.getOrientation(outR,orValues)

        val strBuild = StringBuilder()
        strBuild.append(("方位角（アジマス）:"))
        strBuild.append(rad2Deg(orValues[0]))
        strBuild.append("\n")
        strBuild.append("傾斜角(ピッチ):")
        strBuild.append(rad2Deg(orValues[1]))
        strBuild.append("\n")
        strBuild.append("回転角(ロール):")
        strBuild.append(rad2Deg(orValues[2]))
        strBuild.append("\n")
        binding.txt01.text = strBuild.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * 画面がフォアグラウンドになり、ユーザの操作を受け付けるときに呼び出される
     * 加速度センサーと地磁気センサーの登録
     */
    public override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //加速度センサー
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //地磁気センサー
        val magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        //登録処理
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,magField,SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * ユーザがアクティビティを離れるときに用いる
     */
    public override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //使い終わったら登録を解除する
        sensorManager.unregisterListener(this)
    }

    /**
     * 角度を取得する
     */
    private fun rad2Deg(rad:Float): Int{
        //Math.toDegreesは角度を計測する
        return Math.floor(Math.toDegrees(rad.toDouble())).toInt()
    }
}