package com.king.app.video.data.personal;

public class PersonalDataServiceFactory {

	public static PersonalDataService create() {
		return new PersonalSqldroidDAO();
	}
}
