package by.shyshaliaksey.webproject.model.service.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import by.shyshaliaksey.webproject.controller.FolderPath;
import by.shyshaliaksey.webproject.controller.command.Feedback;
import by.shyshaliaksey.webproject.exception.DaoException;
import by.shyshaliaksey.webproject.exception.ServiceException;
import by.shyshaliaksey.webproject.model.dao.AlienDao;
import by.shyshaliaksey.webproject.model.dao.DaoProvider;
import by.shyshaliaksey.webproject.model.dao.UserDao;
import by.shyshaliaksey.webproject.model.entity.Alien;
import by.shyshaliaksey.webproject.model.entity.Role;
import by.shyshaliaksey.webproject.model.entity.User;
import by.shyshaliaksey.webproject.model.service.AdminService;
import by.shyshaliaksey.webproject.model.service.ServiceProvider;
import by.shyshaliaksey.webproject.model.service.TimeService;
import by.shyshaliaksey.webproject.model.service.UtilService;
import by.shyshaliaksey.webproject.model.service.ValidationService;
import by.shyshaliaksey.webproject.model.util.localization.LocaleKey;
import jakarta.servlet.http.Part;

public class AdminServiceImpl implements AdminService {

	@Override
	public Map<Feedback.Key, Object> banUser(String userLogin, String daysToBan, String currentUser) throws ServiceException {
		try {
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			validationService.validateBanFormInput(result, userLogin, daysToBan);
			if (Boolean.TRUE.equals(result.get(Feedback.Key.LOGIN_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.DAYS_TO_BAN_STATUS))) {
				UserDao userDao = DaoProvider.getInstance().getUserDao();
				Optional<User> user = userDao.findByLogin(userLogin);
				if (user.isPresent() && !currentUser.equals(userLogin)) {
					TimeService timeService = ServiceProvider.getInstance().getTimeService();
					String banDate = timeService.prepareBanDate(Integer.parseInt(daysToBan));
					boolean banUserResult = userDao.banUser(userLogin, banDate);
					if (banUserResult) {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.TRUE);
						result.put(Feedback.Key.DAYS_TO_BAN_STATUS, Boolean.TRUE);
						result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.DAYS_TO_BAN_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.DAYS_TO_BAN_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.CANT_FIND_SUITABLE_USER.getValue());
						result.put(Feedback.Key.DAYS_TO_BAN_FEEDBACK, LocaleKey.CANT_FIND_SUITABLE_USER.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.CANT_FIND_SUITABLE_USER.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when banning user " + userLogin + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> unbanUser(String userLogin, String currentUser) throws ServiceException {
		try {
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			UserDao userDao = DaoProvider.getInstance().getUserDao();
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			validationService.validateLoginFormInput(result, userLogin);

			if (Boolean.TRUE.equals(result.get(Feedback.Key.LOGIN_STATUS))) {
				Optional<User> user = userDao.findByLogin(userLogin);
				if (user.isPresent() && !currentUser.equals(userLogin)) {
					TimeService timeService = ServiceProvider.getInstance().getTimeService();
					String unbanDate = timeService.prepareBanDate(0);
					boolean unbanUserResult = userDao.unbanUser(userLogin, unbanDate);
					if (unbanUserResult) {
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.TRUE);
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
						result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.CANT_FIND_SUITABLE_USER.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.CANT_FIND_SUITABLE_USER.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when unbanning user " + userLogin + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> promoteUser(String userLogin, String currentUserLogin) throws ServiceException {
		try {
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			validationService.validateLoginFormInput(result, userLogin);
			if (Boolean.TRUE.equals(result.get(Feedback.Key.LOGIN_STATUS))) {
				if (!userLogin.equals(currentUserLogin)) {
					UserDao userDao = DaoProvider.getInstance().getUserDao();
					Optional<User> user = userDao.findByLogin(userLogin);
					if (user.isPresent() && user.get().getRole() == Role.USER) {
						boolean promotingResult = userDao.promoteUser(userLogin);
						if (promotingResult) {
							result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
							result.put(Feedback.Key.LOGIN_STATUS, Boolean.TRUE);
							result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						} else {
							result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.INTERNAL_SERVER_ERROR);
							result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
							result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.STANDARD_LOGIN_FEEDBACK.getValue());
						}
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.LOGIN_FEEDBACK,
								LocaleKey.LOGIN_FEEDBACK_INVALID_CAN_NOT_FIND_USER_FOR_PROMOTING.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.LOGIN_FEEDBACK,
							LocaleKey.LOGIN_FEEDBACK_INVALID_PROMOTE_YOURSELF.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when promoting user " + userLogin + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> demoteAdmin(String adminLogin, String currentAdminLogin) throws ServiceException {
		try {
			UserDao userDao = DaoProvider.getInstance().getUserDao();
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			validationService.validateLoginFormInput(result, currentAdminLogin);
			if (Boolean.TRUE.equals(result.get(Feedback.Key.LOGIN_STATUS))) {
				if (!adminLogin.equals(currentAdminLogin)) {
					Optional<User> user = userDao.findByLogin(adminLogin);
					if (user.isPresent() && user.get().getRole() == Role.ADMIN) {
						boolean demotingResult = userDao.demoteAdmin(adminLogin);
						if (demotingResult) {
							result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
							result.put(Feedback.Key.LOGIN_STATUS, Boolean.TRUE);
							result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						} else {
							result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.INTERNAL_SERVER_ERROR);
							result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
							result.put(Feedback.Key.LOGIN_FEEDBACK, LocaleKey.STANDARD_LOGIN_FEEDBACK.getValue());
						}
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
						result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.LOGIN_FEEDBACK,
								LocaleKey.LOGIN_FEEDBACK_INVALID_CAN_NOT_FIND_ADMIN_FOR_DEMOTING.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.LOGIN_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.LOGIN_FEEDBACK,
							LocaleKey.LOGIN_FEEDBACK_INVALID_DEMOTE_YOURSELF.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when demoting admin " + adminLogin + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> addNewAlien(String alienName, String alienSmallDescription,
			String alienFullDescription, Part alienImage, String rootFolder, String serverDeploymentPath)
			throws ServiceException {
		try {
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			UtilService utilService = ServiceProvider.getInstance().getUtilService();
			AlienDao alienDao = DaoProvider.getInstance().getAlienDao();
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			validationService.validateAlienInfoFormInput(result, alienName, alienSmallDescription, alienFullDescription);
			String fileName = alienImage.getSubmittedFileName();			
			validationService.validateImageFormInput(result, FilenameUtils.getExtension(fileName), alienImage.getSize());
			
			if (Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_NAME_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_SMALL_DESCRIPTION_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_FULL_DESCRIPTION_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.IMAGE_STATUS))) {
				Optional<Alien> alienInDatabase = alienDao.findByName(alienName);
				if (!alienInDatabase.isPresent()) {
					String newFileName = utilService.prepareAlienImageName(fileName);
					String imageUrl = FolderPath.ALIEN_IMAGE_FOLDER.getValue() + newFileName;
					boolean uploadToRoot = utilService.uploadImage(rootFolder, FolderPath.ALIEN_IMAGE_FOLDER.getValue(),
							newFileName, alienImage);
					boolean uploadToDeployment = utilService.uploadImage(serverDeploymentPath,
							FolderPath.ALIEN_IMAGE_FOLDER.getValue(), newFileName, alienImage);
					int alienId = alienDao.addNewAlien(alienName, alienSmallDescription, alienFullDescription,
							imageUrl);
					boolean addToGaleryResult = alienDao.addNewImageToGalery(alienId, imageUrl);
					if (uploadToRoot && addToGaleryResult && uploadToDeployment) {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
						result.put(Feedback.Key.ALIEN_NAME_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.IMAGE_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.INTERNAL_SERVER_ERROR);
						result.put(Feedback.Key.ALIEN_NAME_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.IMAGE_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_NAME_FEEDBACK, LocaleKey.INTERNAL_SERVER_ERROR.getValue());
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_FEEDBACK,
								LocaleKey.INTERNAL_SERVER_ERROR.getValue());
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_FEEDBACK,
								LocaleKey.INTERNAL_SERVER_ERROR.getValue());
						result.put(Feedback.Key.IMAGE_FEEDBACK, LocaleKey.INTERNAL_SERVER_ERROR.getValue());
					}

				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.ALIEN_NAME_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.ALIEN_NAME_FEEDBACK,
							LocaleKey.ALIEN_NAME_FEEDBACK_INVALID_ALREADY_EXISTS.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when adding new alien " + alienName + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> updateAlienInfo(int alienId, String alienName, String alienSmallDescription,
			String alienFullDescription) throws ServiceException {
		try {
			UtilService utilService = ServiceProvider.getInstance().getUtilService();
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			AlienDao alienDao = DaoProvider.getInstance().getAlienDao();
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			validationService.validateAlienInfoFormInput(result, alienName, alienSmallDescription,
					alienFullDescription);

			if (Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_NAME_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_SMALL_DESCRIPTION_STATUS))
					&& Boolean.TRUE.equals(result.get(Feedback.Key.ALIEN_FULL_DESCRIPTION_STATUS))) {
				Optional<Alien> alienInDatabase = alienDao.findById(alienId);
				if (alienInDatabase.isPresent()) {
					boolean addResult = alienDao.updateAlienInfo(alienId, alienName, alienSmallDescription,
							alienFullDescription);
					if (addResult) {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
						result.put(Feedback.Key.ALIEN_NAME_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.INTERNAL_SERVER_ERROR);
						result.put(Feedback.Key.ALIEN_NAME_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.ALIEN_NAME_FEEDBACK, LocaleKey.INTERNAL_SERVER_ERROR.getValue());
						result.put(Feedback.Key.ALIEN_SMALL_DESCRIPTION_FEEDBACK,
								LocaleKey.INTERNAL_SERVER_ERROR.getValue());
						result.put(Feedback.Key.ALIEN_FULL_DESCRIPTION_FEEDBACK,
								LocaleKey.INTERNAL_SERVER_ERROR.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.ALIEN_NAME_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.ALIEN_NAME_FEEDBACK,
							LocaleKey.ALIEN_NAME_FEEDBACK_INVALID_DOES_NOT_EXIST.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when adding new alien " + alienName + " :" + e.getMessage(), e);
		}
	}

	@Override
	public Map<Feedback.Key, Object> updateAlienImage(int alienId, Part alienImage, String rootFolder,
			String serverDeploymentPath, String websiteUrl) throws ServiceException {
		try {
			UtilService utilService = ServiceProvider.getInstance().getUtilService();
			ValidationService validationService = ServiceProvider.getInstance().getValidationService();
			AlienDao alienDao = DaoProvider.getInstance().getAlienDao();
			Map<Feedback.Key, Object> result = new EnumMap<>(Feedback.Key.class);
			String fileName = alienImage.getSubmittedFileName();			
			validationService.validateImageFormInput(result, FilenameUtils.getExtension(fileName), alienImage.getSize());
			if (Boolean.TRUE.equals(result.get(Feedback.Key.IMAGE_STATUS))) {
				Optional<Alien> alienInDatabase = alienDao.findById(alienId);
				if (alienInDatabase.isPresent()) {
					String newFileName = utilService.prepareAlienImageName(fileName);
					String imageUrl = FolderPath.ALIEN_IMAGE_FOLDER.getValue() + newFileName;
					boolean uploadToRoot = utilService.uploadImage(rootFolder, FolderPath.ALIEN_IMAGE_FOLDER.getValue(),
							newFileName, alienImage);
					boolean uploadToDeployment = utilService.uploadImage(serverDeploymentPath,
							FolderPath.ALIEN_IMAGE_FOLDER.getValue(), newFileName, alienImage);
					boolean addResult = alienDao.updateAlienImage(alienId, imageUrl);
					boolean addToGaleryResult = alienDao.addNewImageToGalery(alienId, imageUrl);
					if (uploadToRoot && uploadToDeployment && addResult && addToGaleryResult) {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.OK);
						result.put(Feedback.Key.IMAGE_FEEDBACK, LocaleKey.EMPTY_MESSAGE.getValue());
						result.put(Feedback.Key.IMAGE_PATH, websiteUrl + imageUrl);
					} else {
						result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.INTERNAL_SERVER_ERROR);
						result.put(Feedback.Key.IMAGE_STATUS, Boolean.FALSE);
						result.put(Feedback.Key.IMAGE_FEEDBACK, LocaleKey.INTERNAL_SERVER_ERROR.getValue());
					}
				} else {
					result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
					result.put(Feedback.Key.IMAGE_STATUS, Boolean.FALSE);
					result.put(Feedback.Key.IMAGE_FEEDBACK, LocaleKey.ALIEN_NAME_FEEDBACK_INVALID_DOES_NOT_EXIST.getValue());
				}
			} else {
				result.put(Feedback.Key.RESPONSE_CODE, Feedback.Code.WRONG_INPUT);
			}
			return result;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when updating alien image " + " :" + e.getMessage(), e);
		}
	}

	@Override
	public boolean approveAlien(String alienIdString) throws ServiceException {
		try {
			int alienId = Integer.parseInt(alienIdString);
			AlienDao alienDao = DaoProvider.getInstance().getAlienDao();
			boolean approvingResult = alienDao.approveAlien(alienId);
			Optional<Alien> alien = alienDao.findById(alienId);
			String imageUrl = alien.get().getImageUrl();
			boolean galleryAddResult = alienDao.addNewImageToGalery(alienId, imageUrl);
			return approvingResult && galleryAddResult;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when approving alien " + alienIdString + " :" + e.getMessage(),
					e);
		}
	}

	@Override
	public boolean declineAlien(String alienIdString) throws ServiceException {
		int alienId = Integer.parseInt(alienIdString);
		try {
			AlienDao adminDao = DaoProvider.getInstance().getAlienDao();
			boolean approvingResult = adminDao.declineAlien(alienId);
			boolean changeProfileStatus = adminDao.declineProfileImage(alienId);
			return approvingResult && changeProfileStatus;
		} catch (DaoException e) {
			throw new ServiceException("Error occured when declining alien " + alienIdString + " :" + e.getMessage(),
					e);
		}
	}

	@Override
	public boolean approveAlienImage(String alienImageUrl) throws ServiceException {
		try {
			AlienDao adminDao = DaoProvider.getInstance().getAlienDao();
			boolean changeImageStatus = adminDao.approveSuggestedImage(alienImageUrl);
			return changeImageStatus;
		} catch (DaoException e) {
			throw new ServiceException(
					"Error occured when approving alien image " + alienImageUrl + " :" + e.getMessage(), e);
		}
	}

	@Override
	public boolean declineAlienImage(String alienImageUrl) throws ServiceException {
		try {
			AlienDao adminDao = DaoProvider.getInstance().getAlienDao();
			boolean changeImageStatus = adminDao.declineSuggestedImage(alienImageUrl);
			return changeImageStatus;
		} catch (DaoException e) {
			throw new ServiceException(
					"Error occured when declining alien image " + alienImageUrl + " :" + e.getMessage(), e);
		}
	}

}
