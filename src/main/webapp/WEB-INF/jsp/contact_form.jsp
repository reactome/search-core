<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="contact-us-div">

    <form id="contact_form" action="${pageContext.request.contextPath}/contact" method="post" class="contact-form">
        <h4>Please report to us and we will get back shortly.</h4>
        <input type="hidden" name="source" id="source" value="${param.source}"/>
        <input type="hidden" name="exception" id="exception" value="${exception}"/>
        <input type="hidden" name="url" id="url" value="${url}"/>

        <div class="field">
            <label for="contactName"><p>Name*:</p></label>
            <input type="text" id="contactName" name="contactName" size="80" class="search" placeholder="your name"/>
            <span id="contact-name-req"></span>
        </div>
        <div class="field">
            <label for="mailAddress"><p>From*:</p></label>
            <input type="email" id="mailAddress" name="mailAddress" size="80" class="search" placeholder="your-mail@domain.com"/>
            <span id="mail-req"></span>
        </div>
        <div class="field">
            <label for="to"><p>To:</p></label>
            <input type="text" id="to" name="to" size="80" class="search" value="Reactome Helpdesk" readonly>
        </div>
        <div class="field">
            <label for="subject"><p>Subject:</p></label>
            <c:choose>
                
                <c:when test="${not empty subject}">
                    <input type="text" id="subject" name="subject" class="search" value="${subject}" readonly/>
                </c:when>
                <c:otherwise>
                    <input type="text" id="subject" name="subject" class="search" value="URL Bad Request" readonly/>
                </c:otherwise>
            </c:choose>
            <%--<input type="text" id="subject" name="subject" class="search" value="${subject}" readonly/>--%>
        </div>
        <div class="fieldarea">
            <label for="message"><p>Message*:</p></label>
            <c:choose>

                <c:when test="${not empty message}">
                    <textarea id="message" name="message" class="search" rows="5" cols="80">${message}</textarea>
                </c:when>
                <c:otherwise>
                    <textarea id="message" name="message" class="search" rows="5" cols="80">Dear Helpdesk,&#13;&#10;Reactome does not process my request properly.&#13;&#10;Thank you&#13;&#10;</textarea>
                </c:otherwise>
            </c:choose>
            <span id="message-req"></span>
        </div>
        <div style="margin-top: 7px; padding-left: 20px;">
            <input type="checkbox" id="sendEmailCopy" name="sendEmailCopy" checked />
            <span>Send me a copy</span>
        </div>
        <div class="button-send">
            <p>
                <input type="button" class="submit" value="Send" id="send"/>
            </p>
        </div>
    </form>
    <p><span id="msg"/></p>

</div>
<script type="text/javascript">
    $(document).ready(function () {
        $('#send').click(function () {
            var email = $('#mailAddress').val();
            var ok = true;
            if($('#contactName').val() == "" ) {
                $("#contact-name-req").replaceWith("<span id='contact-name-req'>Your name is required</span>");
                $("#contact-name-req").addClass("contact-msg-error");
                ok = false;
            }else {
                $("#contact-name-req").replaceWith("<span id='contact-name-req'></span>");
            }
            if(email == "" ) {
                $("#mail-req").replaceWith("<span id='mail-req'>Email Address is required</span>");
                $("#mail-req").addClass("contact-msg-error");
                ok = false;
            }else {
                $("#mail-req").replaceWith("<span id='mail-req'></span>");
            }
            if($('#message').val() == "" ) {
                $("#message-req").replaceWith("<span id='message-req'>Message is required</span>");
                $("#message-req").addClass("contact-msg-error");
                ok = false;
            }else {
                $("#message-req").replaceWith("<span id='message-req'></span>");
            }

            if(!isEmailValid(email)){
                ok = false;
            }

            if(ok){
                $("#contact-name-req").replaceWith("<span id='contact-name-req'></span>");
                $("#mail-req").replaceWith("<span id='mail-req'></span>");
                $("#message-req").replaceWith("<span id='message-req'></span>");

                $('#send').prop("disabled", true);
                var formData = $("#contact_form");
                $.ajax({
                    url: formData.attr("action"),
                    type: "POST",
                    data: formData.serialize(),
                    success: function (data, textStatus, jqXHR) {
                        formData.remove();

                        $("#msg").replaceWith("<span id='msg'><h5>Thank you for contacting us.&nbsp;We will get back to you shortly.</h5></span>");
                        $("#msg").addClass("contact-msg-success");
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        $('#send').prop("disabled", false);
                        $("#msg").replaceWith("<span id='msg'>Could not send your email. Try again or Please email us at <a href='mailto:help@reactome.org'>help@reactome.org</a></span>");
                        $("#msg").addClass("contact-msg-error");
                    }
                });
            }
        });
    });

    function isEmailValid(mail){
        var status = true;
        var emailRegEx = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i;
        if(mail != "" ) {
            if (mail.search(emailRegEx) == -1) {
                $("#mail-req").replaceWith("<span id='mail-req'>Malformed Email Address.</span>");
                $("#mail-req").addClass("contact-msg-error");
                status = false;
            }
        }
        return status;
    }
</script>
