<#-- @ftlvariable name="entries" type="kotlin.collections.List<com.jetbrains.handson.website.BlogEntry>" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Approving Questions</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<h1>Questions from MPS Slack Community </h1>
<#list questionEntries as question>
    <hr>
    <a href="https://testisky.slack.com/archives/CBQPEPSA2/p${question.timestamp}"><h2>The Question is: ${question.text}</h2></a>

    <#list filesEntries as files>
        <#if question.timestamp == files.fileEntry_timestamp_question>
            <img id="1" alt="question files" src="${files.base64Files}" style="max-height: 300px; max-width: 300px;"/>
        </#if>
    </#list>
    <#list answerEntries as answers>
        <#if question.timestamp == answers.timestamp>
            <div>
                <h5>${answers.text}</h5>
                <#list filesEntries_answers as files_answers>
                    <#if question.timestamp == files_answers.fileEntry_timestamp_answers>
                        <img id="2" alt="answer files" src="${files_answers.base64Files}" style="max-height: 300px; max-width: 300px;"/>
                    </#if>
                </#list>
            </div>
        </#if>
    </#list>

    <form action="/submit" method="post">
    <input type="checkbox" id="posted" name="question_timestamp" value="${question.timestamp}">
        <label for="posted"> Posting?</label>
    <input type="submit" value="Submit">
</form>
</#list>

<hr>
</body>
</html>