package cn.wandersnail.common;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * date: 2019/8/7 11:50
 * author: zengfansheng
 */
public class FileUtils {
    /**
     * 格式化文件大小，根据文件大小不同使用不同单位
     *
     * @param size   文件大小
     * @param format 数字的格式
     * @return 字符串形式的大小，包含单位(B,KB,MB,GB,TB,PB)
     */
    public static String formatFileSize(long size, DecimalFormat format) {
        if (format == null) {
            format = new DecimalFormat("#.00");
        }
        if (size < 1024) {
            return size + " B";
        } else if (size < 1048576) {
            return format.format((size / 1024d)) + " KB";
        } else if (size < 1073741824) {
            return format.format((size / 1048576d)) + " MB";
        } else if (size < 1099511627776L) {
            return format.format((size / 1073741824d)) + " GB";
        } else if (size < 1125899906842624L) {
            return format.format((size / 1099511627776d)) + " TB";
        } else if (size < 1152921504606846976L) {
            return format.format((size / 1125899906842624d)) + " PB";
        } else {
            return "size: out of range";
        }
    }

    /**
     * 格式化文件大小，根据文件大小不同使用不同单位
     *
     * @param size 文件大小
     * @return 字符串形式的大小，包含单位(B,KB,MB,GB,TB,PB)
     */
    public static String formatFileSize(long size) {
        return formatFileSize(size, null);
    }

    /**
     * 返回去掉扩展名的文件名
     */
    public static String deleteSuffix(String fileName) {
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 获取扩展名
     *
     * @param s 路径或后缀
     * @return 不存在后缀时返回null
     */
    public static String getSuffix(String s) {
        if (s != null && s.contains(".")) {
            return s.substring(s.lastIndexOf("."));
        }
        return null;
    }

    /**
     * 从路径中获取文件名，包含扩展名
     *
     * @param path 路径
     * @return 如果所传参数是合法路径，截取文件名，如果不是返回原值
     */
    public static String getFileName(String path) {
        return getFileName(path, false);
    }

    /**
     * 从路径中获取文件名
     *
     * @param path          路径
     * @param withoutSuffix true不包含扩展名，false包含
     * @return 如果所传参数是合法路径，截取文件名，如果不是返回原值
     */
    public static String getFileName(String path, boolean withoutSuffix) {
        if ((path.contains("/") || path.contains("\\"))) {
            int beginIndex = path.lastIndexOf("\\");
            String fileName = path;
            if (beginIndex != -1) {
                fileName = fileName.substring(beginIndex + 1);
            }
            beginIndex = fileName.lastIndexOf("/");
            if (beginIndex != -1) {
                fileName = fileName.substring(beginIndex + 1);
            }
            return withoutSuffix ? deleteSuffix(fileName) : fileName;
        }
        return withoutSuffix ? deleteSuffix(path) : path;
    }

    /**
     * 检查是否有同名文件，有则在自动在文件名后加当前时间的毫秒值
     */
    public static File checkAndRename(File target) {
        Objects.requireNonNull(target, "target is null");
        if (target.exists()) {
            String fileName = target.getName();
            if (fileName.contains(".")) {
                String sub = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = fileName.replace(sub, StringUtils.randomUuid() + "_" + sub);
            } else {
                fileName = StringUtils.randomUuid() + "_" + fileName;
            }
            return new File(target.getParent(), fileName);
        }
        return target;
    }

    /**
     * 去掉字符串中重复部分字符串
     *
     * @param dup  重复部分字符串
     * @param strs 要去重的字符串
     * @return 按参数先后顺序返回一个字符串数组
     */
    public static String[] removeDuplicate(String dup, String... strs) {
        String[] out = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            out[i] = strs[i].replace(dup + "+", "");
        }
        return out;
    }

    /**
     * 获取随机UUID文件名
     *
     * @param fileName 原文件名
     * @return 生成的文件名
     */
    public static String generateRandonFileName(String fileName) {
        return StringUtils.randomUuid() + getSuffix(fileName);
    }

    /**
     * 使用GZIP压缩数据
     */
    public static byte[] compressByGZIP(byte[] bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream out = new GZIPOutputStream(baos);
            out.write(bytes);
            out.finish();
            out.flush();
            out.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return bytes;
        }
    }

    /**
     * 使用GZIP压缩文件
     *
     * @param src    待压缩文件
     * @param target 压缩后文件
     */
    public static void compressByGZIP(File src, File target) {
        compressByGZIP(src, target, 40960);
    }

    /**
     * 使用GZIP压缩文件
     *
     * @param src        待压缩文件
     * @param target     压缩后的文件
     * @param bufferSize 读取流时的缓存大小
     */
    public static void compressByGZIP(File src, File target, int bufferSize) {
        if (!src.exists()) return;
        FileInputStream fis = null;
        GZIPOutputStream out = null;
        try {
            fis = new FileInputStream(src);
            out = new GZIPOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.finish();
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis, out);
        }
    }

    /**
     * 从流保存到文件，不会关闭输入流
     *
     * @param target 目标文件
     */
    public static void toFile(InputStream inputStream, File target) {
        toFile(inputStream, target, 40960);
    }

    /**
     * 从流保存到文件，不会关闭输入流
     *
     * @param target     目标文件
     * @param bufferSize 读取流时的缓存大小
     */
    public static void toFile(InputStream inputStream, File target, int bufferSize) {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(target));
            BufferedInputStream in = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 复制文件
     * @param src 源文件
     * @param target 目标文件
     */
    public static void copyFile(File src, File target) {
        copyFile(src, target, 40960);
    }

    /**
     * 复制文件
     * @param src 源文件
     * @param target 目标文件
     * @param bufferSize 读取流时的缓存大小
     */
    public static void copyFile(File src, File target, int bufferSize) {
        if (!src.exists()) return;
        BufferedInputStream fis = null;
        BufferedOutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(src));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis, fos);
        }
    }

    /**
     * 复制文件夹
     * @param srcDir 源文件夹
     * @param targetDir 目标文件夹
     */
    public static void copyDir(File srcDir, File targetDir) {
        copyDir(srcDir, targetDir, 40960);
    }

    /**
     * 复制文件夹
     * @param srcDir 源文件夹
     * @param targetDir 目标文件夹
     * @param bufferSize 读取流时的缓存大小
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyDir(File srcDir, File targetDir, int bufferSize) {
        //目标目录新建源文件夹
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        // 获取源文件夹当前下的文件或目录   
        File[] files = srcDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    copyFile(file, new File(targetDir, file.getName()), bufferSize);
                } else {
                    copyDir(file, new File(targetDir, file.getName()), bufferSize);
                }
            }
        }
    }

    /**
     * 复制文件或文件夹
     * @param src 源文件或文件夹
     * @param target 目标文件或文件夹
     */
    public static void copy(File src, File target) {
        copy(src, target, 40960);
    }
    
    /**
     * 复制文件或文件夹
     * @param src 源文件或文件夹
     * @param target 目标文件或文件夹
     * @param bufferSize 读取流时的缓存大小
     */
    public static void copy(File src, File target, int bufferSize) {
        if (src.exists()) {
            if (src.isFile()) {
                copyFile(src, target, bufferSize);
            } else {
                copyDir(src, target, bufferSize);
            }
        }
    }

    /**
     * 获取文件或文件夹大小
     */
    public static long getSize(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                return file.length();
            } else {
                long size = 0;
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        if (f.isFile()) {
                            size += f.length();
                        } else {
                            size += getSize(f);
                        }
                    }
                }
                return size;
            }
        } else {
            return 0;
        }
    }

    /**
     * 删除文件夹，包含自身
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDir(File dir) {
        emptyDir(dir);
        dir.delete();
    }

    /**
     * 删除文件夹内所有文件
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void emptyDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteDir(file);
                }
            }
        }
    }

    /*
     * 比较大小并删除源
     */
    private static boolean compareAndDeleteSrc(File src, File target) {
        //如果文件存在，并且大小与源文件相等，则写入成功，删除源文件或文件夹
        if (src.exists()) {
            if (src.isFile()) {
                if (target.exists() && target.length() == src.length()) {
                    return src.delete();
                }
            } else {
                if (getSize(src) == getSize(target)) {
                    deleteDir(src);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 移动文件或文件夹
     *
     * @param target 目标文件或文件夹。类型需与源相同，如源为文件，则目标也必须是文件
     * @return 移动成功返回true, 否则返回false
     */
    public static boolean move(File src, File target) {
        return move(src, target, 40960, true);
    }

    /**
     * 移动文件或文件夹
     *
     * @param target     目标文件或文件夹。类型需与源相同，如源为文件，则目标也必须是文件
     * @param bufferSize 读取流时的缓存大小
     * @return 移动成功返回true, 否则返回false
     */
    public static boolean move(File src, File target, int bufferSize) {
        return move(src, target, bufferSize, true);
    }

    /**
     * 移动文件或文件夹
     *
     * @param target  目标文件或文件夹。类型需与源相同，如源为文件，则目标也必须是文件
     * @param replace 当有重名文件时是否替换。传false时，自动在重命名
     * @return 移动成功返回true, 否则返回false
     */
    public static boolean move(File src, File target, boolean replace) {
        return move(src, target, 40960, replace);
    }

    /**
     * 移动文件或文件夹
     *
     * @param target     目标文件或文件夹。类型需与源相同，如源为文件，则目标也必须是文件
     * @param bufferSize 读取流时的缓存大小
     * @param replace    当有重名文件时是否替换。传false时，自动在重命名
     * @return 移动成功返回true, 否则返回false
     */
    public static boolean move(File src, File target, int bufferSize, boolean replace) {
        if (!src.exists()) return false;
        if (!replace) {
            target = checkAndRename(target);
        }
        if (src.isFile()) {
            copyFile(src, target, bufferSize);
        } else {
            copyDir(src, target, bufferSize);
        }
        return compareAndDeleteSrc(src, target);
    }
}
