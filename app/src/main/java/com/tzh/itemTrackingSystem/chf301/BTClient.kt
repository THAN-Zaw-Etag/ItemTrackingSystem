package com.tzh.itemTrackingSystem.chf301

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import com.tzh.itemTrackingSystem.service.BluetoothService
import java.util.Locale

@SuppressLint("MissingPermission")
object BTClient {
    lateinit var mBluetoothLeService: BluetoothService
    var RecvBuff = ByteArray(5000)
    var RecvLength = 0
    private var time1: Long = 0
    var ComAddr: Byte = 0
    var CmdTime: Long = 500
    var DeviceName = ""
    var tagid = ""
    var CmdIng = false
    var ActiveModeStr = ""
    fun GetDevName(): String {
        return DeviceName
    }

    fun init_com(Baudrate: Byte, Parity: Byte): String {
        val data = ByteArray(7)
        data[0] = 0
        data[1] = 7
        data[2] = 1
        data[3] = Baudrate
        data[4] = Parity
        getCRC(data, 5)
        return bytesToHexString(data, 0, 7)
    }

    fun SetDevName(name: String) {
        DeviceName = name
    }

    fun settag_id(name: String) {
        tagid = name
    }

    fun gettag_id(): String {
        return tagid
    }

    fun getCRC(data: ByteArray, Len: Int) {
        var i: Int
        var j: Int
        var current_crc_value = 0xFFFF
        i = 0
        while (i < Len) {
            current_crc_value = current_crc_value xor (data[i].toInt() and 0xFF)
            j = 0
            while (j < 8) {
                current_crc_value =
                    if (current_crc_value and 0x01 != 0) current_crc_value shr 1 xor 0x8408 else current_crc_value shr 1
                j++
            }
            i++
        }
        data[i++] = (current_crc_value and 0xFF).toByte()
        data[i] = (current_crc_value shr 8 and 0xFF).toByte()
    }

    fun CheckCRC(data: ByteArray?, len: Int): Boolean {
        val daw = ByteArray(256)
        memcpy(data, 0, daw, 0, len)
        getCRC(daw, len)
        return 0 == daw[len + 1].toInt() && 0 == daw[len].toInt()
    }

    fun ArrayClear(Msg: ByteArray, Size: Int) {
        for (i in 0 until Size) {
            Msg[i] = 0
        }
    }

    fun memcpy(SourceByte: ByteArray?, StartBit_1: Int, Targetbyte: ByteArray, StartBit_2: Int, Length: Int) {
        for (m in 0 until Length) {
            Targetbyte[StartBit_2 + m] = SourceByte!![StartBit_1 + m]
        }
    }

    fun memcpy(SourceByte: ByteArray, Targetbyte: ByteArray, Length: Int) {
        for (m in 0 until Length) {
            Targetbyte[m] = SourceByte[+m]
        }
    }

    fun bytesToHexString(src: ByteArray, offset: Int, length: Int): String {
        var stmp = ""
        val sb = StringBuilder()
        for (n in 0 until length) {
            stmp = Integer.toHexString(src[n + offset].toInt() and 0xFF)
            sb.append(if (stmp.length == 1) "0$stmp" else stmp)
        }
        return sb.toString().uppercase(Locale.getDefault()).trim { it <= ' ' }
    }

    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexString = hexString
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return d
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    fun GetData(): Int {
        time1 = System.currentTimeMillis()
        while (System.currentTimeMillis() - time1 < 2000) {
            SystemClock.sleep(20)
            val recvLen = mBluetoothLeService.RecvString!!.length / 2
            if (recvLen > 0) {
                var buffer: ByteArray? = ByteArray(recvLen)
                buffer = hexStringToBytes(mBluetoothLeService.RecvString)
                memcpy(buffer, 0, RecvBuff, 0, recvLen)
                RecvLength = recvLen
                val activelen = buffer!![0] + 1
                if (CheckCRC(RecvBuff, activelen)) {
                    Log.d("read data:", mBluetoothLeService.RecvString!!)
                    CmdIng = false
                    return 0
                }
            }
        }
        CmdIng = false
        return -1
    }

    fun GetReaderInfo(Version: ByteArray, Power: ByteArray, Fre: ByteArray): Int {
        val Msg = ByteArray(6)
        Msg[0] = 0x04
        Msg[1] = 0xFF.toByte()
        Msg[2] = 0x21
        Msg[3] = 0x19
        Msg[4] = 0x95.toByte()
        CmdTime = 500
        mBluetoothLeService.target_chara!!.value = Msg
        ArrayClear(RecvBuff, 5000)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            Version[0] = RecvBuff[4]
            Version[1] = RecvBuff[5]
            Power[0] = RecvBuff[10]
            Fre[0] = RecvBuff[8]
            Fre[1] = RecvBuff[9]
            return RecvBuff[3].toInt()
        }
        return -1
    }

    fun SetPower(Pwr: Byte): Int {
        val Msg = ByteArray(6)
        Msg[0] = 0x05
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 0x2F
        Msg[3] = Pwr
        getCRC(Msg, 4)
        CmdTime = 500
        mBluetoothLeService.target_chara!!.value = Msg
        ArrayClear(RecvBuff, 5000)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            return RecvBuff[3].toInt()
        }
        return -1
    }

    fun SetRegion(MaxFre: Byte, MinFre: Byte): Int {
        val Msg = ByteArray(7)
        Msg[0] = 0x06
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 0x22
        Msg[3] = MaxFre
        Msg[4] = MinFre
        getCRC(Msg, 5)
        CmdTime = 500
        mBluetoothLeService.target_chara!!.value = Msg
        ArrayClear(RecvBuff, 300)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            return RecvBuff[3].toInt()
        }
        return -1
    }

    fun SetBaudRate(BaudRate: Byte): Int {
        val Msg = ByteArray(10)
        Msg[0] = 0x05
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 0x28
        Msg[3] = BaudRate
        getCRC(Msg, 4)
        CmdTime = 500
        mBluetoothLeService.target_chara!!.value = Msg
        ArrayClear(RecvBuff, 300)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            return RecvBuff[3].toInt()
        }
        return -1
    }

    fun GetInventoryData(): Int {
        time1 = System.currentTimeMillis()
        while (System.currentTimeMillis() - time1 < 3000) {
            SystemClock.sleep(10)
            val recvLen = mBluetoothLeService.RecvString!!.length / 2
            if (recvLen > 0) {
                var buffer: ByteArray? = ByteArray(recvLen)
                buffer = hexStringToBytes(mBluetoothLeService.RecvString)
                memcpy(buffer, 0, RecvBuff, 0, recvLen)
                RecvLength = recvLen
                val Buff1 = ByteArray(5000)
                memcpy(RecvBuff, 0, Buff1, 0, recvLen)
                var nTurn = recvLen
                while (nTurn > 0) {
                    val Buff2 = ByteArray(5000)
                    if (nTurn < (Buff1[0].toInt() and 255) + 1) {
                        break
                    }
                    if (Buff1[3].toInt() != 0x03 && Buff1[3].toInt() != 0x04 && (Buff1[0].toInt() and 255) + 1 == nTurn) {
                        CmdIng = false
                        return 0
                    }
                    nTurn = nTurn - (Buff1[0].toInt() and 255) - 1
                    memcpy(Buff1, (Buff1[0].toInt() and 255) + 1, Buff2, 0, nTurn)
                    ArrayClear(Buff1, 5000)
                    memcpy(Buff2, 0, Buff1, 0, nTurn)
                }
            }
        }
        CmdIng = false
        return -1
    }

    fun Inventory_G2(
        QValue: Byte,
        Session: Byte,
        AdrTID: Byte,
        LenTID: Byte,
        TIDFlag: Byte,
        CardNum: IntArray,
        EPCList: ByteArray,
        EPCLength: IntArray
    ): Int {
        val Msg = ByteArray(10)
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 1
        Msg[3] = QValue
        Msg[4] = Session
        if (TIDFlag.toInt() == 0) {
            Msg[0] = 6
            getCRC(Msg, 5)
        } else {
            Msg[0] = 8
            Msg[5] = AdrTID
            Msg[6] = LenTID
            getCRC(Msg, 7)
        }
        mBluetoothLeService.target_chara!!.value = Msg
        ArrayClear(RecvBuff, 300)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
        Log.d("Inventory_G2", "start Inventory TRUE")
        if (GetInventoryData() == 0) {
            val szBuff = ByteArray(3000)
            val szBuff1 = ByteArray(3000)
            memcpy(RecvBuff, 0, szBuff, 0, RecvLength)
            var Nlen = 0
            while (RecvLength > 0) {
                val nLenszBuff = (szBuff[0].toInt() and 255) + 1
                if (szBuff[3].toInt() == 0x01 || szBuff[3].toInt() == 0x02 || szBuff[3].toInt() == 0x03 || szBuff[3].toInt() == 0x04) {
                    //*Ant=szBuff[4];
                    CardNum[0] += szBuff[5].toInt() and 255
                    memcpy(szBuff, 6, szBuff1, Nlen, (szBuff[0].toInt() and 255) - 7)
                    Nlen += (szBuff[0].toInt() and 255) - 7
                    if (RecvLength - (szBuff[0].toInt() and 255) - 1 > 0) {
                        val daw = ByteArray(3000)
                        memcpy(szBuff, (szBuff[0].toInt() and 255) + 1, daw, 0, RecvLength - szBuff[0] * 255 - 1)
                        ArrayClear(szBuff, 3000)
                        memcpy(daw, 0, szBuff, 0, RecvLength - szBuff[0] * 255 - 1)
                    }
                }
                RecvLength = RecvLength - nLenszBuff
            }
            memcpy(szBuff1, 0, EPCList, 0, Nlen)
            EPCLength[0] = Nlen
            return szBuff[3].toInt()
        }
        return 0x30
    }

    fun ReadData_G2(Enum: Byte, EPC: ByteArray, Mem: Byte, WordAddr: Byte, Num: Byte, Psd: ByteArray, Data: ByteArray): Int {
        val Msg = ByteArray(200)
        Msg[0] = (12 + Enum * 2).toByte()
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 0x02
        Msg[3] = Enum
        for (i in 0 until Enum * 2) {
            Msg[4 + i] = EPC[i]
        }
        Msg[4 + Enum * 2] = Mem
        Msg[5 + Enum * 2] = WordAddr
        Msg[6 + Enum * 2] = Num
        Msg[7 + Enum * 2] = Psd[0]
        Msg[8 + Enum * 2] = Psd[1]
        Msg[9 + Enum * 2] = Psd[2]
        Msg[10 + Enum * 2] = Psd[3]
        getCRC(Msg, 11 + Enum * 2)
        CmdTime = 500
        var Len = 13 + Enum * 2
        var SendFlag = true
        while (SendFlag) {
            if (Len - 20 > 0) {
                val data = ByteArray(20)
                memcpy(Msg, 0, data, 0, 20)
                val daw = ByteArray(200)
                memcpy(Msg, 20, daw, 0, Len - 20)
                memcpy(daw, 0, Msg, 0, Len - 20)
                Len = Len - 20
                mBluetoothLeService.target_chara!!.value = data
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
                Log.d("write:", bytesToHexString(data, 0, 20))
            } else {
                val data = ByteArray(Len)
                memcpy(Msg, 0, data, 0, Len)
                mBluetoothLeService.target_chara!!.value = data
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
                Log.d("write:", bytesToHexString(data, 0, Len))
                SendFlag = false
            }
        }
        ArrayClear(RecvBuff, 300)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            if (RecvBuff[3].toInt() == 0) {
                memcpy(RecvBuff, 4, Data, 0, RecvLength - 6)
                return 0
            }
            return -1
        }
        return -1
    }

    fun WriteData_G2(Enum: Byte, EPC: ByteArray, Mem: Byte, WordAddr: Byte, WNum: Byte, Psd: ByteArray, Data: ByteArray): Int {
        val Msg = ByteArray(200)
        Msg[0] = (12 + Enum * 2 + WNum * 2).toByte()
        Msg[1] = (ComAddr.toInt() and 255).toByte()
        Msg[2] = 0x03
        Msg[3] = WNum
        Msg[4] = Enum
        for (i in 0 until Enum * 2) {
            Msg[5 + i] = EPC[i]
        }
        Msg[5 + Enum * 2] = Mem
        Msg[6 + Enum * 2] = WordAddr
        for (i in 0 until WNum * 2) {
            Msg[7 + Enum * 2 + i] = Data[i]
        }
        Msg[7 + Enum * 2 + WNum * 2] = Psd[0]
        Msg[8 + Enum * 2 + WNum * 2] = Psd[1]
        Msg[9 + Enum * 2 + WNum * 2] = Psd[2]
        Msg[10 + Enum * 2 + WNum * 2] = Psd[3]
        getCRC(Msg, 11 + Enum * 2 + WNum * 2)
        CmdTime = 500
        var Len = 13 + Enum * 2 + WNum * 2
        var SendFlag = true
        while (SendFlag) {
            if (Len - 20 > 0) {
                val data = ByteArray(20)
                memcpy(Msg, 0, data, 0, 20)
                val daw = ByteArray(200)
                memcpy(Msg, 20, daw, 0, Len - 20)
                memcpy(daw, 0, Msg, 0, Len - 18)
                Len = Len - 20
                mBluetoothLeService.target_chara!!.value = data
                Log.d("write:", bytesToHexString(data, 0, 20))
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
            } else {
                val data = ByteArray(Len)
                memcpy(Msg, 0, data, 0, Len)
                mBluetoothLeService.target_chara!!.value = data
                Log.d("write:", bytesToHexString(data, 0, Len))
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.target_chara)
                SendFlag = false
            }
        }
        ArrayClear(RecvBuff, 300)
        RecvLength = 0
        mBluetoothLeService.RecvString = ""
        if (GetData() == 0) {
            ComAddr = RecvBuff[1]
            return if (RecvBuff[3].toInt() == 0) {
                0
            } else -1
        }
        return -1
    }
}