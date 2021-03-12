package com.yanxing.biometriclibrary

import java.util.*

/**
 * @author 李双祥 on 2021/3/11.
 */

/**
 * hex字符串转byte数组
 *
 * @param inHex 待转换的Hex字符串
 * @return 转换后的byte数组结果
 */
fun hexToByteArray(hexString: String): ByteArray{
    var inHex = hexString
    var hexlen = inHex.length
    val result: ByteArray
    if (hexlen % 2 == 1) {
        // 奇数
        hexlen++
        result = ByteArray(hexlen / 2)
        inHex = "0$inHex"
    } else {
        // 偶数
        result = ByteArray(hexlen / 2)
    }
    var j = 0
    var i = 0
    while (i < hexlen) {
        result[j] = hexToByte(inHex.substring(i, i + 2))
        j++
        i += 2
    }
    return result
}

fun hexToByte(inHex: String): Byte {
    return inHex.toInt(16).toByte()
}

/**
 * 转十六进制
 */
fun bytesToHexString(bArr: ByteArray): String {
    val sb = StringBuffer(bArr.size)
    var sTmp: String
    for (i in bArr.indices) {
        sTmp = Integer.toHexString(0xFF and bArr[i].toInt())
        if (sTmp.length < 2) sb.append(0)
        sb.append(sTmp.toUpperCase(Locale.getDefault()))
    }
    return sb.toString()
}