<script type="text/javascript">

function fillAccount(){
    document.getElementByName("email").value = '%username%';
    document.getElementByName("password").value = '%password%';
    document.getElementByClassName("btn btn-primary anim-blue-all").click();
}

function getAccount(){
    window.loginjs.setAccount(document.getElementById("userId").value, document.getElementById("passwd").value);
}

</script>