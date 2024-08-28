function init() {
	changeTitle();

	$("#crudGeneratorForm").validate({
        rules: {
            directory: {
                required: true
            },
            packageName: {
                required: true,
                pattern: "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$"
            },
            entityName: {
                required: true,
                pattern: "^[a-zA-Z_$][a-zA-Z0-9_$]*$"
            },
            jpaPackageName: {
                required: true,
                pattern: "^(?:[a-zA-Z_][a-zA-Z0-9_]*\\.)*[a-zA-Z_][a-zA-Z0-9_]*$"
            },
        },
        messages: {
            directory: {
                required: "Directory is required"
            },
            packageName: {
                required: "Package name is required",
                pattern: "Please enter a valid package name"
            },
            EntityName: {
                required: "Entity name is required",
                pattern: "Please enter a valid entity name"
            },
            jpaPackageName: {
                required: "JPA package name is required",
                pattern: "Please enter a valid JPA package name"
            }
        }
    });

    $(document).ready(function () {
	    // Liquibase fields visibility logic
	    $(document).on('change', 'input[name^="generateLiquibase"]', function() {
            $('#liquibaseAuditorNameField').toggleClass('hidden');
            $('#databaseSchemaField').toggleClass('hidden');
        });

        $('.field-to-hide').addClass('hidden');
        $('.string-fields').removeClass('hidden');

        $(document).on('change', 'input[name^="id"]', function() {
            var $propertiesBlock = $(this).closest('.properties-block');

            if ($(this).is(':checked')) {
                $propertiesBlock.find('.parentTypeField, .typeField, .dbField, .field-to-hide').addClass('hidden');
            } else {
                $propertiesBlock.find('.typeField').find('select').each(function() {this.selectedIndex = 0;});
                $propertiesBlock.find('.parentTypeField, .typeField, .dbField, .string-fields').removeClass('hidden');
            }
        });

        $(document).on('change', 'select[name^="type"]', function() {
            var $propertiesBlock = $(this).closest('.properties-block');
            var selectedType = $(this).val();

            // Hide all fields initially
            $propertiesBlock.find('.field-to-hide').addClass('hidden');
            $propertiesBlock.find('input[type="text"], select').not('select[name="type"]').val('');
            $propertiesBlock.find('input[type="checkbox"]').prop('checked', false);
            $propertiesBlock.find('select').not('select[name="type"]').each(function() {
                this.selectedIndex = 0;
            });

            // Show specific fields based on selected type

            //string-fields enum-fields
            switch (selectedType) {
                case 'String':
                    $propertiesBlock.find('.string-fields').removeClass('hidden');
                    break;
                case 'Integer':
                case 'Long':
                    $propertiesBlock.find('.minField, .maxField, .numericValidation').removeClass('hidden');
                    break;
                case 'BigDecimal':
                    $propertiesBlock.find('.precisionField, .scaleField').removeClass('hidden');
                    break;
                case 'Enum':
                    $propertiesBlock.find('.columnLengthField, .enumValuesField').removeClass('hidden');
                    break;
                case 'Instant':
                    $propertiesBlock.find('.temporalValidation').removeClass('hidden');
                    break;
                default:
                    break;
            }
        });
    });
}

function addProperty() {
    // Get the properties container and the properties block
    const propertiesContainer = document.getElementById('properties-container');
    const propertiesBlock = document.querySelector('.properties-block');

    // Clone the properties block
    const newPropertiesBlock = propertiesBlock.cloneNode(true);

    // Clear the input values in the new properties block
    newPropertiesBlock.querySelectorAll('input').forEach(input => input.value = '');
    newPropertiesBlock.querySelectorAll('select').forEach(select => select.selectedIndex = 0);
    newPropertiesBlock.querySelectorAll('input[type="checkbox"]').forEach(checkbox => checkbox.checked = false);
    const $newPropertiesBlock = $(newPropertiesBlock);

    $newPropertiesBlock.find('.field-to-hide').addClass('hidden');
    $newPropertiesBlock.find('.parentTypeField, .typeField, .dbField, .string-fields').removeClass('hidden');

    // Append the new properties block to the properties container
    propertiesContainer.appendChild(newPropertiesBlock);
}

function deletePropertyBlock(button) {
    if ($('#properties-container').find('.properties-block').length>1)
        $(button).closest('.properties-block').remove();
}

function clearForm() {
    // Clear all input fields
    $('#crudGeneratorForm input[type="text"], #crudGeneratorForm select').val('');
    $('#crudGeneratorForm input[type="checkbox"]').prop('checked', true);

    // Reset all select fields to their default option
    $('#crudGeneratorForm select').each(function() {
        this.selectedIndex = 0;
    });

    // Remove all properties blocks except the first one
    $('#properties-container .properties-block').not(':first').remove();

    // Ensure the first property block's fields are also reset
    $('#properties-container .properties-block:first input[type="text"], #properties-container .properties-block:first select').val('');
    $('#properties-container .properties-block:first input[type="checkbox"]').prop('checked', false);
    $('#properties-container .properties-block:first select').each(function() {
        this.selectedIndex = 0;
    });

    const $newPropertiesBlock = $('#properties-container .properties-block:first');

    $newPropertiesBlock.find('.field-to-hide').addClass('hidden');
    $newPropertiesBlock.find('.parentTypeField, .typeField, .dbField, .string-fields').removeClass('hidden');

}

function handleCrudGeneration() {
	
	if (!$("#crudGeneratorForm").valid())
		return;

	console.log(JSON.stringify(getFormData($("form[name='crudGeneratorForm']"))));

	var form = document.getElementById('crudGeneratorForm');
    var formData = new FormData(form);

    var data = {
        directory: formData.get('directory'),
        packageName: formData.get('packageName'),
        entityName: formData.get('entityName'),
        databaseSchema: formData.get('databaseSchema'),
        tablePrefix: formData.get('tablePrefix'),
        generateLiquibase: formData.get('generateLiquibase') === 'true',
        liquibaseAuditorName: formData.get('liquibaseAuditorName'),
        generateAuditSection: formData.get('generateAuditSection') === 'true',
        jpaPackageName: formData.get('jpaPackageName'),
        properties: []
    };

    var propertiesBlocks = document.querySelectorAll('.properties-block');
    propertiesBlocks.forEach(function(block) {
        var property = {
            name: block.querySelector('input[name="name"]').value,
            type: block.querySelector('select[name="type"]').value,
            id: block.querySelector('input[name="id"]').checked,
            required: block.querySelector('input[name="required"]').checked,
            unique: block.querySelector('input[name="unique"]').checked,
            indexable: block.querySelector('input[name="indexable"]').checked,
            enumValues: block.querySelector('input[name="enumValues"]').value,
            pattern: block.querySelector('input[name="pattern"]').value,
            min: block.querySelector('input[name="min"]').value,
            max: block.querySelector('input[name="max"]').value,
            precision: block.querySelector('input[name="precision"]').value,
            scale: block.querySelector('input[name="scale"]').value,
            positive: block.querySelector('input[name="positive"]').checked,
            positiveOrZero: block.querySelector('input[name="positiveOrZero"]').checked,
            negative: block.querySelector('input[name="negative"]').checked,
            negativeOrZero: block.querySelector('input[name="negativeOrZero"]').checked,
            future: block.querySelector('input[name="future"]').checked,
            futureOrPresent: block.querySelector('input[name="futureOrPresent"]').checked,
            past: block.querySelector('input[name="past"]').checked,
            pastOrPresent: block.querySelector('input[name="pastOrPresent"]').checked,
            email: block.querySelector('input[name="email"]').checked,
            parentType: block.querySelector('select[name="parentType"]').value
        };

        // Remove any empty properties
        for (var key in property) {
            if (property[key] === "" || property[key] === false) {
                delete property[key];
            }
        }

        data.properties.push(property);
    });
	
	$('#loader').show(0);
	$.ajax({
		type : "POST",
		url : "/api/crudGenerator",
        data : JSON.stringify(data),
		dataType : "json",
		contentType : "application/json; charset=utf-8",
		beforeSend : function(xhr) {
			/* Authorization header */
			//xhr.setRequestHeader("Authorization", basicAuth);
			//xhr.setRequestHeader("X-Mobile", "false");
		},
		success : function(data) {
		    console.log("Success:");
			$('#loader').hide(0);
			displayAlertPopup('fa-check-circle', 'text-green-600', "Success", function() {$("#alertPopup").toggleClass('hidden');});
		},
		error : function(data) {
			console.log(data);
			$('#loader').hide(0);
			handleApplicationError(data);
		}
	});
}
	