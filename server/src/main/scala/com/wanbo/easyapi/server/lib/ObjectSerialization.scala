package com.wanbo.easyapi.server.lib

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import org.apache.commons.codec.binary.Base64
import org.slf4j.LoggerFactory

/**
 * Object serialization
 * Created by Wanbo.Ge<gewanbo@gmail.com> on 15-1-6.
 */
object ObjectSerialization {

    private val logger = LoggerFactory.getLogger(ObjectSerialization.getClass.getSimpleName)

    /**
     * Encode a serializable object to base64 string.
     * @param obj serializable object
     * @return base64 string
     */
    def objectEncode(obj: Serializable): String = {
        var retData = ""

        try {
            val aos = new ByteArrayOutputStream()
            val gos = new GZIPOutputStream(aos)
            val oos = new ObjectOutputStream(aos)
            oos.writeObject(obj)
            oos.flush()
            oos.close()
            gos.close()
            aos.close()

            retData = new String(Base64.encodeBase64(aos.toByteArray))
        } catch {
            case e: Exception =>
                logger.error("Object encode error:", e)
        }

        retData
    }

    /**
     * Decode a object from base64 string.
     * @param str the serialization base64 string of the object
     * @return object
     */
    def objectDecode(str: String): Object = {
        var retObj = new Object
        try {
            val ais = new ByteArrayInputStream(Base64.decodeBase64(str))
            val gis = new GZIPInputStream(ais)
            val ois = new ObjectInputStream(ais)
            retObj = ois.readObject()

            ois.close()
            gis.close()
            ais.close()
        } catch {
            case e: Exception =>
                logger.error("Object decode error:", e)
        }

        retObj
    }
}