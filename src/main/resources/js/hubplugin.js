function performTestConnection(mode) {
    updateMode("test");
            
    $form = AJS.$("#configForm");
    $form.submit();
}
        
function ensureSubmit() {
    updateMode("submit");
}
        
function updateMode(mode) {
	$field = AJS.$("#submitMode");
    $field.value = mode;
}