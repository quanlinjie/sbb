package com.study.sbb.service;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.study.sbb.entity.SiteUser;
import com.study.sbb.handler.DataNotFoundException;
import com.study.sbb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	@Value("${ImgLocation}")
	private String imgLocation;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public SiteUser create(String username, String email, String password, String nickname, MultipartFile file)
			throws Exception {

		String profileImage = imgLocation;

		UUID uuid = UUID.randomUUID();
		String ImageName = uuid + "_" + file.getOriginalFilename();
		File saveFile = new File(profileImage, ImageName);
		file.transferTo(saveFile);

		SiteUser user = new SiteUser();
		user.setUsername(username);
		user.setNickname(nickname); // 닉네임 추가
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setImageName(ImageName);
		user.setProfileImage(profileImage + ImageName);
		this.userRepository.save(user);
		return user;
	}

	public SiteUser getUser(String username) { // User 서비스를 통해 SiteUser를 조회할 수 있는 getUser 메서드
		Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
		if (siteUser.isPresent()) {
			return siteUser.get();
		} else {
			throw new DataNotFoundException("siteuser not found");
		}
	}

	// 회원수정
	public SiteUser modify(SiteUser siteUser, String nickname, String email, MultipartFile file) throws Exception {

		String projectPath = imgLocation;

		if (file.getOriginalFilename().equals(" ")) {
			// 새 파일이 없을 때
			siteUser.setImageName(siteUser.getImageName());
			siteUser.setProfileImage(siteUser.getProfileImage());

		} else if (file.getOriginalFilename() != null) {
			// 새 파일이 있을 때
			File f = new File(siteUser.getProfileImage());

			if (f.exists()) { // 파일이 존재하면
				f.delete(); // 파일 삭제
			}

			UUID uuid = UUID.randomUUID();
			String ImageName = uuid + "_" + file.getOriginalFilename();
			File saveFile = new File(projectPath, ImageName);
			file.transferTo(saveFile);

			siteUser.setImageName(ImageName);
			siteUser.setProfileImage(projectPath + ImageName);
		}

		siteUser.setNickname(nickname);
		siteUser.setEmail(email);
		this.userRepository.save(siteUser);
		return siteUser;
	}
}