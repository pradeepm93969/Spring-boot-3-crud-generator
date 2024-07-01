function changeTitle() {
	var pageTitle = $("#page-title-value").text().toString().replaceAll('\n','').replaceAll('\t','');
	if (pageTitle != "")
		$("#page-title").text(" | " + pageTitle);

	$(document).attr('title', $("#main-title").text());
}

function loadPage(name, queryString) {
	
	if (name.startsWith('#')) {
		name = name.substr(1);
	}
	
	const jsUrl = 'js' + (name.includes('?') ? name.split('?')[0] : name) + '.js';
	window.location.hash=name;
	$.ajax({
        type: "GET",
        url: '/page' + name,
        dataType: "html",
        contentType: "application/json; charset=utf-8",
        beforeSend: function (xhr) {
            /* Authorization header */
            //xhr.setRequestHeader("Authorization", basicAuth);
            //xhr.setRequestHeader("X-Mobile", "false");
        },
        success: function (data) {
        	$('#page-wrapper').html(data);
        	changeTitle();
        	$('#mainModal').html($('.modal-container').html());
        	$.getScript(jsUrl).done(function(script, textStatus) {
        		if (queryString != undefined) 
        			window.history.pushState({}, document.title, window.location.pathname + window.location.hash + queryString);
        		else
        			window.history.pushState({}, document.title, window.location.pathname + window.location.hash);
        			
        		init();
        	});
        	
        },
        error: function (data) {
            if (data.status == 401 && window.location.pathname != "/") {
            	localStorage.setItem('token','');
            	window.location.href = "/?logout";
            }
        }
   });
}

function displayAlertPopup(icon, iconColor, message, clickFunction) {
	$('#alertPopup .modal-header').html('<i class="fas fa-5x ' + icon + ' ' + iconColor + '"></i>');
	$('#alertPopup #alertMessage').html(message);
	$('#alertPopup .modal-footer button').off('click');
	$('#alertPopup .modal-footer button').click(clickFunction);
	$("#alertPopup").toggleClass('hidden');
}

function handleApplicationError(data) {
    $('#alertPopup .modal-header').html('<i class="fas fa-5x fa-times-circle text-danger"></i>');
    var errorMessage = data.responseJSON.message + "<br/>";
    if (data.responseJSON.errors !== undefined && data.responseJSON.errors !== "" && data.responseJSON.errors.length > 0) {
        for (var i = 0; i < data.responseJSON.errors.length; i++) {
            errorMessage += data.responseJSON.errors[i].field + " : " + data.responseJSON.errors[i].defaultMessage + "<br/>"
        }
    }
    displayAlertPopup('fa-times-circle', 'text-red-600', errorMessage, function() {console.log(errorMessage);$("#alertPopup").toggleClass('hidden');});
}

function getTimeZoneOffset() {
    var t = new Date().toString().match(/[-\+]\d{4}/)[0];
    return ":00.000" + t.substring(0,3) + ":" + t.substr(3);
}

function getFormData(formElement) {
	var rawJson = formElement.serializeArray();
	var model = {};

	$.map(rawJson, function (n, i) {
		if (n['value'] !== "" && (formElement.find(":input[name=" + n['name'] + "]").attr('type') === "number" 
			|| formElement.find(":input[name=" + n['name'] + "]").attr('type') === "tel"))
			model[n['name']] = Number(n['value']);
		else if (n['value'] !== "" && (formElement.find(":input[name=" + n['name'] + "]").attr('type') === "datetime-local"))
			model[n['name']] = n['value'].slice(0,16).concat(getTimeZoneOffset());
		else
			model[n['name']] = n['value'];
	});
	
	formElement.find('input[type=checkbox]:not(:checked)').map(function(){
		model[this.name] = this.title;
	});

	return model;
}


function serializeFormData(formName) {
	var formSerData = $("form[name=" + formName + "]").serialize().split("&");
	var searchData = "";
	var keyValue;
	for (var i=0; i<formSerData.length; i++) {
		keyValue = formSerData[i].split("=");
		if ($("form[name=" + formName + "] input[name=" + keyValue[0] + "]").attr("type") == "datetime-local"
				&& keyValue[1] !== undefined && keyValue[1] !== "") {
			searchData += keyValue[0] + "=" + keyValue[1] + encodeURIComponent(getTimeZoneOffset()) + "&";
		} else {
			searchData += keyValue[0] + "=" + keyValue[1] + "&";
		}
	}
	return searchData;
}

function loadFormData(formElement, data) {
	$.map(data, function (v, k) {
		if (formElement.find(":input[name=" + k + "]").attr('type') !== undefined) {
			if (formElement.find(":input[name=" + k + "]").attr('type') == "datetime-local")
				formElement.find(":input[name=" + k + "]").val(v.slice(0,19));
			else if (formElement.find(":input[name=" + k + "]").attr('type') == "checkbox")
				formElement.find(":input[name=" + k + "]").prop('checked', v);
			else
				formElement.find(":input[name=" + k + "]").val(v);
		}
		if (formElement.find("select[name=" + k + "]").attr('name') !== undefined) {
			formElement.find("select[name=" + k + "]").val(v.toString());
		}
	});
}

/*function  getObjectFormFields(form) {

    var result = {};
    var arrayAuxiliar = [];
    form.find(":input:text").each(function (index, element) {
        result[$(element).attr('name')] = $(element).val();
    });
    form.find(":input[type=hidden]").each(function (index, element) {
    	if ($(element).attr('name') === 'mobileNumberCountryCode')
    		result[$(element).attr('name')] = Number($(element).val());
    	else 
    		result[$(element).attr('name')] = $(element).val();
    });
    form.find(":input[type=number]").each(function (index, element) {
    	result[$(element).attr('name')] = Number($(element).val());
    });
    form.find(":input[type=tel]").each(function (index, element) {
    	result[$(element).attr('name')] = Number($(element).val());
    });
    form.find(":input:checked").each(function (index, element) {
    	var name;
        var value;
        if ($(this).attr("type") == "radio") {
        	result[$(element).attr('name')] = Number($(element).val());
        }
        else if ($(this).attr("type") == "checkbox") {
            name = $(element).attr('name');
            value = $(element).val();
            if (result[name])
            {
                if (Array.isArray(result[name]))
                {
                    result[name].push(value);
                } else
                {
                    var aux = result[name];
                    result[name] = [];
                    result[name].push(aux);
                    result[name].push(value);
                }

            } else
            {
                result[name] = [];
                result[name].push(value);
            }
        }

    });
    form.find("select option:selected").each(function (index, element) {
        result[$(element).parent().attr('name')] = $(element).val();
    });

    arrayAuxiliar = [];
    form.find("checkbox:checked").each(function (index, element)
    {
        var name = $(element).attr('name');
        var value = $(element).val();
        result[name] = arrayAuxiliar.push(value);
    });

    form.find("textarea").each(function (index, element)
    {
        var name = $(element).attr('name');
        var value = $(element).val();
        result[name] = value;
    });

    return result;
}*/

jQuery.validator.setDefaults({
    onfocusout: function (e) {
        this.element(e);
    },
    onkeyup: false,

    highlight: function (element) {
        jQuery(element).addClass('border-red-500');
    },
    unhighlight: function (element) {
        jQuery(element).removeClass('border-red-500');
        jQuery(element).addClass('border-green-600');
    },

    errorElement: 'div',
    errorClass: 'invalid-tooltip',
    errorPlacement: function (error, element) {
        error.insertAfter(element);
    },
});

/*$.validator.addMethod("email", function(value, element) {
	return this.optional(element) || /^.+@goli.pk$/.test(value);
},"Only company emails are allowed.");*/

$.validator.addMethod("checkLower", function(value) {
  return /[a-z]/.test(value);
},"Password must contain atleast 1 lower case letter");
$.validator.addMethod("checkUpper", function(value) {
  return /[A-Z]/.test(value);
},"Password must contain atleast 1 upper case letter");
$.validator.addMethod("checkDigit", function(value) {
  return /[0-9]/.test(value);
},"Password must contain atleast 1 lower case letter");
$.validator.addMethod("checkSpecial", function	(value) {
  return /[*@#$&_\-]/.test(value);
},"Password must contain atleast 1 special letter(*,@,#,$,&,_,-)");
