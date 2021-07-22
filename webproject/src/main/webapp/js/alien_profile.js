import { AlienForm } from "./modules/alien.js";
import { Comment } from "./modules/comment.js";
import { initializeCarousel } from "./modules/carousel.js";
import { pagination } from "./modules/pagination.js";
import { rating } from "./modules/raiting.js";
window.pagination = pagination;
window.rating = rating;

let formAlienUpdateInfo;
let formAlienUpdateInfoName;
let formAlienUpdateInfoDescriptionSmall;
let formAlienUpdateInfoDescriptionFull;

let formAlienUpdateInfoNameInvalidFeedback;
let formAlienUpdateInfoDescriptionSmallInvalidFeedback;
let formAlienUpdateInfoDescriptionFullInvalidFeedback;

let formAlienUpdateImage;
let formAlienUpdateImageImage;
    
let formAlienUpdateImageImageInvalidFeedback;
let formAlienUpdateImageImageLabel;

let formNewComment;
let formNewCommentComment;
let formNewCommentCommentInvalidFeedback;

let alienId;
let userId;

let deleteCommentButtons;

/** @type {AlienForm} */
let alienForm;
/** @type {Comment} */
let commentForm;

$(document).ready(function () {
    formAlienUpdateInfo = document.getElementById("form-alien-update-info");
    formAlienUpdateInfoName = document.getElementById("form-alien-update-info-name");
    formAlienUpdateInfoDescriptionSmall = document.getElementById("form-alien-update-info-description-small");
    formAlienUpdateInfoDescriptionFull = document.getElementById("form-alien-update-info-description-full");
    
    formAlienUpdateInfoNameInvalidFeedback = document.getElementById("form-alien-update-info-name-invalid-feedback");
    formAlienUpdateInfoDescriptionSmallInvalidFeedback = document.getElementById("form-alien-update-info-description-small-invalid-feedback");
    formAlienUpdateInfoDescriptionFullInvalidFeedback = document.getElementById("form-alien-update-info-description-full-invalid-feedback");

    formAlienUpdateImage = document.getElementById("form-alien-update-image");
    formAlienUpdateImageImage = document.getElementById("form-alien-update-image-image");
    
    formAlienUpdateImageImageInvalidFeedback = document.getElementById("form-alien-update-image-image-invalid-feedback");
    formAlienUpdateImageImageLabel = document.getElementById("form-alien-update-image-image-label");

    alienForm = new AlienForm(formAlienUpdateInfo, formAlienUpdateInfoName, formAlienUpdateInfoDescriptionSmall, 
        formAlienUpdateInfoDescriptionFull, formAlienUpdateImageImage, formAlienUpdateImageImageLabel, formAlienUpdateInfoNameInvalidFeedback, 
        formAlienUpdateInfoDescriptionSmallInvalidFeedback, formAlienUpdateInfoDescriptionFullInvalidFeedback, formAlienUpdateImageImageInvalidFeedback);

    alienId = document.getElementById("alien-id-hidden").innerHTML;
    userId = document.getElementById("currentUserId").innerText;

    formNewComment = document.getElementById("form-new-comment");
    formNewCommentComment = document.getElementById("form-new-comment-comment");
    formNewCommentCommentInvalidFeedback = document.getElementById("form-new-comment-comment-invalid-feedback");

    commentForm = new Comment(formNewComment, formNewCommentComment, formNewCommentCommentInvalidFeedback);
    deleteCommentButtons = document.getElementsByName("delete-comment-button");

});

function updateAlienInfo() {
    let data = {};
    data[ALIEN_ID] = alienId;
    data[ALIEN_NAME] = formAlienUpdateInfoName.value;
    data[ALIEN_SMALL_DESCRIPTION] = formAlienUpdateInfoDescriptionSmall.value;
    data[ALIEN_FULL_DESCRIPTION] = formAlienUpdateInfoDescriptionFull.value;
    let url = CONTROLLER + "?" + COMMAND + "=" + UPDATE_ALIEN_INFO + "&" + ALIEN_ID + "=" + alienId;
    $.ajax({
        url: url,
        type: "POST",
        data: data,
        dataType: "json",
        success: function (data, textStatus, jqXHR) {
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alienForm.removeInfoValidationClasses();
            alienForm.setFeedbackInfo(jqXHR.responseJSON[ALIEN_NAME_STATUS], jqXHR.responseJSON[ALIEN_SMALL_DESCRIPTION_STATUS],
                jqXHR.responseJSON[ALIEN_FULL_DESCRIPTION_STATUS], jqXHR.responseJSON[ALIEN_NAME_FEEDBACK], 
                jqXHR.responseJSON[ALIEN_SMALL_DESCRIPTION_FEEDBACK], jqXHR.responseJSON[ALIEN_FULL_DESCRIPTION_FEEDBACK]);
        }
    });
};

function updateAlienImage() {
    let alienId = document.getElementById("alien-id-hidden").innerHTML;

    let formData = new FormData();
    formData.append(ALIEN_ID, alienId);
    formData.append(ALIEN_NEW_IMAGE, formAlienUpdateImageImage.files[0]);
    let url = CONTROLLER + "?" + COMMAND + "=" + UPDATE_ALIEN_IMAGE + "&" + ALIEN_ID + "=" + alienId;
    $.ajax({
        url: url,
        type: "POST",
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        dataType: "json",
        success: function (data, textStatus, jqXHR) {
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alienForm.removeImageValidationClasses();
            alienForm.setFeedbackImage(jqXHR.responseJSON[IMAGE_STATUS], jqXHR.responseJSON[IMAGE_FEEDBACK]);
        }
    });
};

function addNewComment() {
    let data = {};
    data[NEW_COMMENT] = formNewCommentComment.value;
    data[ALIEN_ID] = alienId;
    data[USER_ID] = userId;
    let url = CONTROLLER + "?" + COMMAND + "=" + ADD_NEW_COMMENT;
    $.ajax({
        url: url,
        type: "POST",
        data: data,
        success: function (data, textStatus, jqXHR) {
        },
        error: function (jqXHR, textStatus, errorThrown) {
            commentForm.removeCommentValidationClasses();
            commentForm.setCommentFeedback(jqXHR.responseJSON[ALIEN_NAME_STATUS], jqXHR.responseJSON[COMMENT_FEEDBACK]);
        }
    });
};

function deleteComment(commentId) {
    let data = {};
    data[COMMENT_ID] = commentId;
    let url = CONTROLLER + "?" + COMMAND + "=" + DELETE_COMMENT;
    $.ajax({
        url: url,
        type: "POST",
        data: data,
        success: function (data, textStatus, jqXHR) {
            alert("success");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.status + " "  + textStatus + " " + errorThrown);
        }
    });
};

const alienProfile = {
    updateAlienInfo: updateAlienInfo,
    updateAlienImage: updateAlienImage,
    addNewComment: addNewComment,
    deleteComment: deleteComment
}

/**
 * Setting values to alien info input
 */
$(document).ready(function () {
    if (formAlienUpdateInfo != null) {
        formAlienUpdateInfoName.value = document.getElementById("alien-name").innerHTML;
        formAlienUpdateInfoDescriptionSmall.value = document.getElementById("alien-small-description").innerHTML;
        formAlienUpdateInfoDescriptionFull.value = document.getElementById("alien-big-description").innerHTML;
    }
});

/**
 * Alien update info processing
 */
$(document).ready(function () {
    if (formAlienUpdateInfo != null) {
        formAlienUpdateInfo.addEventListener('submit', function(event) {
            alienForm.removeInfoValidationClasses();
            let validationResult = alienForm.validateInfo();

            if (!validationResult[0] || !validationResult[1] || !validationResult[2]) {
                event.preventDefault();
                event.stopPropagation();
            } else {
                event.preventDefault();
                alienProfile.updateAlienInfo()
            }
          }, false);
            formAlienUpdateInfoName.addEventListener('input', function(event) {
                alienForm.removeNameValidationClasses();
            })
            formAlienUpdateInfoDescriptionSmall.addEventListener('input', function(event) {
                alienForm.removeSmallDescriptionValidationClasses();
            })
            formAlienUpdateInfoDescriptionFull.addEventListener('input', function(event) {
                alienForm.removeFullDescriptionValidationClasses();
          })
    }
});

/**
 * Alien update image processing
 */
$(document).ready(function () {
    if (formAlienUpdateImage != null) {
        formAlienUpdateImage.addEventListener('submit', function(event) {
            alienForm.removeImageValidationClasses();
            let validationResult = alienForm.validateImage();
    
            if (!validationResult) {
                alienForm.setFeedbackImage(false, STANDARD_IMAGE_FEEDBACK);
                event.preventDefault();
                event.stopPropagation();
            } else {
                event.preventDefault();
                alienProfile.updateAlienImage()
            }
        }, false);
        
        formAlienUpdateImageImage.addEventListener('input', function(event) {
        alienForm.setLabelText(formAlienUpdateImageImage.files[0].name);
            alienForm.removeImageValidationClasses();
        })
    }
});

/**
 * Add new comment processing
 */
$(document).ready(function () {
    if (formNewComment != null) {
        formNewComment.addEventListener('submit', function(event) {
            commentForm.removeCommentValidationClasses();
            let validationResult = commentForm.validateComment();
            if (!validationResult) {
                commentForm.setCommentFeedback(false, STANDARD_COMMENT_FEEDBACK);
                event.preventDefault();
                event.stopPropagation();
            } else {
                event.preventDefault();
                alienProfile.addNewComment()
            }
          }, false);
         
        formNewCommentComment.addEventListener('input', function(event) {
            commentForm.removeCommentValidationClasses();
        })
    }

});

/**
 * Delete comment event listeners
 */
$(document).ready(function() {
    deleteCommentButtons.forEach(button => 
        button.addEventListener('click', function(event) {
            let commentId = button.parentElement.children[0].innerText;
            alienProfile.deleteComment(commentId);
        })
    );
});

/**
 * Carousel initialization
 */
 $(document).ready(function () {
    initializeCarousel();
});

/**
 * set rating value
 */

document.addEventListener("DOMContentLoaded",() => {
    if(document.getElementById("ratingStars") != null) {
        rating.setRatingValue();
    }
});