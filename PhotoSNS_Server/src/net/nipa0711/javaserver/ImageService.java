package net.nipa0711.javaserver;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import sun.misc.BASE64Encoder;

public class ImageService {

	public static void saveToFile(String base64, String photoFullPath) {
		byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64);
		try {
			BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(imageBytes));
			ImageIO.write(bufImg, "jpg", new File(photoFullPath));
			System.out.println("Saved Image File");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeThumbnail(String OriginalImgPath, String thumbnailPath) {
		try {
			Image image = ImageIO.read(new File(OriginalImgPath));
			int newWidth = 200;
			double imageWidth = image.getWidth(null);
			double imageHeight = image.getHeight(null);

			double ratio = (double) newWidth / (double) imageWidth;
			int w = (int) (imageWidth * ratio);
			int h = (int) (imageHeight * ratio);

			Image resizeImage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);

			// 새 이미지 저장하기
			BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics g = newImage.getGraphics();
			g.drawImage(resizeImage, 0, 0, null);
			g.dispose();
			ImageIO.write(newImage, "jpg", new File(thumbnailPath));
			System.out.println("Created Thumbnail image");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String imageToBase64(String inputFileName) throws IOException {
		BASE64Encoder base64Encoder = new BASE64Encoder();
		InputStream in = new FileInputStream(new File(inputFileName));
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		int len = 0;
		byte[] buf = new byte[1024];
		while ((len = in.read(buf)) != -1) {
			byteOutStream.write(buf, 0, len);
		}
		byte fileArray[] = byteOutStream.toByteArray();
		String encodeString = base64Encoder.encodeBuffer(fileArray);
		return encodeString;
	}
}
