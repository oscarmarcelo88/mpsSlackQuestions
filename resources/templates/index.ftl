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
        <h2>The Question is: ${question.text}</h2>
    <#list answerEntries as answers>
        <#if question.timestamp == answers.timestamp>
            <div>
                <h5>${answers.text}</h5>
            </div>
        </#if>
    </#list>
    <form action="/submit" method="post">
    <input type="checkbox" id="posted" name="question_timestamp" value="${question.timestamp}">
        <label for="posted"> Posting? </label>
    <input type="submit" value="Post">
</form>
</#list>

<hr>
<div>
    <h3>Add a new journal entry!</h3>
    <form action="/submit" method="post">
        <input type="text" name="headline">
        <br>
        <textarea name="body"></textarea>
        <br>
        <input type="submit">
    </form>
</div>
</body>
</html>