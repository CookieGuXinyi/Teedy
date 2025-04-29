package com.sismics.util;

import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestImageUtil {
    private BufferedImage rgbImage;
    private BufferedImage binaryImage;
    private BufferedImage alphaImage;

    @Before
    public void setUp() {
        // 创建不同类型的测试图像
        rgbImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        binaryImage = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_BINARY);
        alphaImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        // 设置像素值
        rgbImage.setRGB(0, 0, 0xFF0000); // 红色
        binaryImage.setRGB(0, 0, 0x000000); // 黑色
        alphaImage.setRGB(0, 0, 0x800000FF); // 半透明蓝色
    }

    @Test
    public void testWriteJpegNoAlpha() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtil.writeJpeg(rgbImage, outputStream);
        assertTrue(outputStream.size() > 0); // 确认生成了 JPEG 数据
    }

    @Test
    public void testWriteJpegWithAlpha() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtil.writeJpeg(alphaImage, outputStream);
        assertTrue(outputStream.size() > 0); // 确认生成了无 alpha 的 JPEG 数据
    }

    @Test
    public void testComputeGravatarNull() {
        String result = ImageUtil.computeGravatar(null);
        assertNull(result); // 测试 null 输入
    }

    @Test
    public void testComputeGravatarValid() {
        String result = ImageUtil.computeGravatar("test@example.com");
        assertEquals("55502f40dc8b7c769880b10874abc9d0", result); // 已知的 MD5 值
    }

    @Test
    public void testIsBlackBinaryImage() {
        assertTrue(ImageUtil.isBlack(binaryImage, 0, 0)); // 测试二值图像的黑色像素
        assertFalse(ImageUtil.isBlack(binaryImage, 1, 1)); // 默认白色
    }

    @Test
    public void testIsBlackRgbImage() {
        assertFalse(ImageUtil.isBlack(rgbImage, 0, 0)); // 红色不认为是黑色
    }

    @Test
    public void testIsBlackOutOfBounds() {
        assertFalse(ImageUtil.isBlack(rgbImage, -1, 0)); // 边界外返回 false
        assertFalse(ImageUtil.isBlack(rgbImage, 10, 10));
    }

}