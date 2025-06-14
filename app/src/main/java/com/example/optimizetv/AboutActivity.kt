package com.example.optimizetv

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import android.view.View
import android.widget.Button
import android.graphics.Bitmap
import android.net.Uri
import android.content.Intent

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val qrCodeView = findViewById<ImageView>(R.id.qr_code)
        val supportLabel = findViewById<TextView>(R.id.tv_support_label)
        val btnClose = findViewById<Button>(R.id.btn_close)

        // Генерация QR-кода
        val url = "https://yoomoney.ru/to/41001983944189" 
        val qrCode = generateQRCode(url, 480)

        if (qrCode != null) {
            qrCodeView.setImageBitmap(qrCode)
            qrCodeView.visibility = View.VISIBLE
        }

        // Обработчик клика по надписи
        supportLabel.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Обработчик кнопки закрытия
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun generateQRCode(text: String, size: Int): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: WriterException) {
            Toast.makeText(this, "Ошибка генерации QR", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
