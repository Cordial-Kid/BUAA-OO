import com.oocourse.spec1.main.VideoInterface;

public class Video implements VideoInterface {
    private int id;
    private int uploadId;

    public Video(int id, int uploadId) {
        this.id = id;
        this.uploadId = uploadId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VideoInterface)) {
            return false;
        }
        return ((VideoInterface) obj).getId() == id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getUploaderId() {
        return uploadId;
    }
}
