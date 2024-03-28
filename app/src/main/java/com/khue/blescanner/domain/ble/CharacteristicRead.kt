package com.khue.blescanner.domain.ble

data class CharacteristicRead(
    val uuid: String = "",
    val value: ByteArray = ByteArray(0),
    val status: Int = -1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacteristicRead

        if (uuid != other.uuid) return false
        if (!value.contentEquals(other.value)) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + status
        return result
    }
}
