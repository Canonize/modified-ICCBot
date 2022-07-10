import android.os.Parcelable;
import android.os.Parcel;

public class MyParcelable implements Parcelable {
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}
}
