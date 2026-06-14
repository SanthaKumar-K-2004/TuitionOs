package com.example.ui.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Bilingual WhatsApp message templates for tuition center communications.
 * Uses wa.me deep links — no Meta Business API needed, no ban risk, no cost.
 */
object WhatsAppTemplates {

    fun feeReminder(
        studentName: String,
        amount: Double,
        dueDate: String,
        outstandingBalance: Double,
        lang: String = "en"
    ): String {
        val message = if (lang == "ta") {
            "அன்புள்ள பெற்றோர், $studentName -ன் கல்விக் கட்டணம் ₹${amount.toInt()} (நிலுவை: ₹${outstandingBalance.toInt()}) தேதி $dueDate வரை செலுத்த வேண்டும். தயவுசெய்து விரைவில் செலுத்துங்கள். நன்றி."
        } else {
            "Dear Parent, this is a reminder that the tuition fee installment for $studentName is ₹${amount.toInt()} (outstanding: ₹${outstandingBalance.toInt()}) due by $dueDate. Kindly process the payment soon. Thank you."
        }
        return encode(message)
    }

    fun feePaidConfirmation(
        studentName: String,
        month: String,
        amount: Double,
        lang: String = "en"
    ): String {
        val message = if (lang == "ta") {
            "அன்புள்ள பெற்றோர், $studentName -ன் $month கட்டணம் ₹${amount.toInt()} வெற்றிகரமாக பெறப்பட்டது. ஒத்துழைப்புக்கு நன்றி!"
        } else {
            "Dear Parent, this is to confirm that the tuition fee for $studentName for $month (₹${amount.toInt()}) has been received. Thank you for your cooperation!"
        }
        return encode(message)
    }

    fun absentNotification(
        studentName: String,
        date: String,
        lang: String = "en"
    ): String {
        val message = if (lang == "ta") {
            "அன்புள்ள பெற்றோர், $studentName இன்று ($date) வகுப்பிற்கு வரவில்லை என்பதை தெரிவிக்கிறோம். தயவுசெய்து காரணத்தை தெரிவிக்கவும். நன்றி."
        } else {
            "Dear Parent, this is to inform you that $studentName was absent today ($date). Please let us know the reason. Thank you."
        }
        return encode(message)
    }

    fun leadFollowUp(
        leadName: String,
        lang: String = "en"
    ): String {
        val message = if (lang == "ta") {
            "வணக்கம் $leadName, உங்கள் சேர்க்கை விசாரணைக்கு நன்றி. TuitionOS-ல் சேர விரும்பினால் எங்களை தொடர்பு கொள்ளுங்கள். நாங்கள் உங்களுக்கு உதவ தயாராக உள்ளோம்!"
        } else {
            "Hello $leadName, thank you for your admission inquiry. We'd love to have you join our tuition center. Feel free to reach out if you have any questions. We're here to help!"
        }
        return encode(message)
    }

    fun attendanceUpdate(
        studentName: String,
        presentCount: Int,
        totalCount: Int,
        percentage: Int,
        lang: String = "en"
    ): String {
        val message = if (lang == "ta") {
            "அன்புள்ள பெற்றோர், $studentName -ன் வருகை விவரம்: $presentCount/$totalCount நாட்கள் ($percentage%). சிறப்பான வருகைக்கு நன்றி!"
        } else {
            "Dear Parent, here's the attendance update for $studentName: $presentCount/$totalCount days ($percentage%). Thank you for maintaining excellent attendance!"
        }
        return encode(message)
    }

    fun buildWaLink(phone: String, encodedMessage: String): String {
        val cleanPhone = phone.replace(" ", "").replace("+", "").replace("-", "")
        val finalPhone = if (cleanPhone.startsWith("91") && cleanPhone.length == 12) cleanPhone
                         else if (cleanPhone.length == 10) "91$cleanPhone"
                         else cleanPhone
        return "https://wa.me/$finalPhone?text=$encodedMessage"
    }

    private fun encode(text: String): String {
        return URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
    }
}
