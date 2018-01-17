package cn.redcdn.hvs.im.collection;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author chencj 文件排序
 */

public class FileSortHelper {

    public enum SortMethod {
        name, size, date, type
    }

    private SortMethod mSort;

    private boolean mFileFirst;

    private HashMap<SortMethod, Comparator> mComparatorList = new HashMap<SortMethod, Comparator>();

    public FileSortHelper() {
        mSort = SortMethod.name;
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        mComparatorList.put(SortMethod.type, cmpType);
    }

    public void setSortMethog(SortMethod s) {
        mSort = s;
    }

    public SortMethod getSortMethod() {
        return mSort;
    }

    public void setFileFirst(boolean f) {
        mFileFirst = f;
    }

    public Comparator getComparator() {
        return mComparatorList.get(mSort);
    }

    private abstract class FileComparator implements
            Comparator<ButeleCollectionFile> {

        @Override
        public int compare(ButeleCollectionFile object1,
                           ButeleCollectionFile object2) {
            if (object1.IsDir == object2.IsDir) {
                return doCompare(object1, object2);
            }

            if (mFileFirst) {
                return (object1.IsDir ? 1 : -1);
            } else {
                return object1.IsDir ? -1 : 1;
            }
        }

        protected abstract int doCompare(ButeleCollectionFile object1,
                                         ButeleCollectionFile object2);
    }

    private Comparator cmpName = new FileComparator() {
        @Override
        public int doCompare(ButeleCollectionFile object1,
                             ButeleCollectionFile object2) {
            return object1.getMtext().compareToIgnoreCase(object2.getMtext());
        }
    };

    @SuppressWarnings("rawtypes")
    private Comparator cmpSize = new FileComparator() {
        @Override
        public int doCompare(ButeleCollectionFile object1,
                             ButeleCollectionFile object2) {
            return longToCompareInt(object1.getFileSize()
                    - object2.getFileSize());
        }
    };

    private Comparator cmpDate = new FileComparator() {
        @Override
        public int doCompare(ButeleCollectionFile object1,
                             ButeleCollectionFile object2) {
            return longToCompareInt(object2.getModifiedDate()
                    - object1.getModifiedDate());
        }
    };

    private int longToCompareInt(long result) {
        return result > 0 ? 1 : (result < 0 ? -1 : 0);
    }

    private Comparator cmpType = new FileComparator() {
        @Override
        public int doCompare(ButeleCollectionFile object1,
                             ButeleCollectionFile object2) {
            int result = getExtFromFilename(object1.getMtext())
                    .compareToIgnoreCase(getExtFromFilename(object2.getMtext()));
            if (result != 0)
                return result;

            return getNameFromFilename(object1.getMtext()).compareToIgnoreCase(
                    getNameFromFilename(object2.getMtext()));
        }
    };

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }
}
