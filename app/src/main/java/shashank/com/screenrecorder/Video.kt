package shashank.com.screenrecorder

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by shashankm on 10/03/17.
 */
data class Video(val data: String, val addedOn: Long, val name: String?, val duration: Long) : Parcelable {

    constructor(source: Parcel) : this(source.readString(), source.readLong(), source.readString(), source.readLong())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(this.data)
        dest?.writeLong(this.addedOn)
        dest?.writeString(this.name)
        dest?.writeLong(this.duration)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(source: Parcel): Video {
                return Video(source)
            }

            override fun newArray(size: Int): Array<Video?> {
                return arrayOfNulls(size)
            }
        }
    }
}