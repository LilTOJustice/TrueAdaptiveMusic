package liltojustice.trueadaptivemusic.client.browser

object DataSizeHelper {
    fun getDataSizeString(sizeInBytes: Long): String {
        val sizeMB = sizeInBytes / 1000000F
        return if (sizeMB > 1000) {
            String.format("%.2f", sizeMB / 1000) + " GB"
        }
        else {
            String.format("%.2f", sizeMB) + " MB"
        }
    }
}