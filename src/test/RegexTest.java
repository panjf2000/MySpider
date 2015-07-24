package test;

public class RegexTest {

	public static void main(String[] args) {
		String url = "http://product.pconline.com.cn/mobile/lg/571029.html";
		String url2 = "http://product.pconline.com.cn/mobile/lg/571029_detail.html";
		String url3 = "http://product.pconline.com.cn/mobile/21ke/c.jpg";
		testImg(url3);
	}

	private static void testIndex(String url) {
//		"(http:|https:)([\\w/.]+)\\d+.html"
		boolean isProductIndex = url.matches("(http:|https:)([\\w/.]+)\\d+.html");
		System.out.println(isProductIndex);
	}
	
	private static void testDetail(String url) {
		boolean isPara = url.endsWith("_detail.html");
		System.out.println(isPara);
	}
	
	private static void testImg(String url) {
		boolean IsImg = url.matches("(http:|https:)([\\w/.]+)(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$");
		System.out.println(IsImg);
	}
}
