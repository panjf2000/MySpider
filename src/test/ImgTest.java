package test;

public class ImgTest {

	private static final String[] imgType = {".jpg",".jpeg",".png",".gif"};
	private static final String regrex = ".*(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$";
	
	public static void main(String[] args) {
		String uri = "http://img.pconline.com.cn/images/product/5731/573193/q_sn.JPG";
		String uri2 = "http://img.pconline.com.cn/images/product/5481/548155/q_m.jpg";
		String uri3 = "http://img.pconline.com.cn/images/product/5731/573193/q_sn.png";
		String uri4= "http://img.pconline.com.cn/images/product/4519/451928/sj_duowei_S630(2)_m.jpg";
		String uri5 = "http://img.pconline.com.cn/images/product/5731/573193/q_sn.PNG";
		String uri6 = "http://img.pconline.com.cn/images/product/4519/451928/sj_duowei_S630%282%29_m.jpg";
		IsImg(uri6);
	}

	private static void IsImg(String uri) {
		boolean isImg = false;
		isImg = uri.matches(regrex);
//		for (String string : imgType) {
//			if(uri.endsWith(string))
//				isImg = true;
//			break;
//		}
		System.out.println(isImg);
	}
}
