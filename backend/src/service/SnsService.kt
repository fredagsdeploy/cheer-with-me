package dev.fredag.cheerwithme.service

import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.services.sns.model.*
import java.util.*
import java.util.regex.Pattern

const val applicationArn = "arn:aws:sns:eu-central-1:601851889032:app/APNS_SANDBOX/cheer-with-me"

class SnsService {
    private val snsClient = AwsSNSFactory.snsClient()
    private var badgeNbr = 0

    fun sendPush(arn: String, message: String) {

        val apnsSandbox = mapOf(
            "aps" to mapOf(
                "alert" to message,
                "badge" to badgeNbr++
            )
        )

        val apnsSandboxJson = ObjectMapper().writeValueAsString(apnsSandbox)

        val messageBody = mapOf(
            "APNS_SANDBOX" to apnsSandboxJson
        )

        val messageJson = ObjectMapper().writeValueAsString(messageBody)
        println(messageJson)

        val publishRequest = PublishRequest.builder()
            .messageStructure("json")
            .message(messageJson)
            .targetArn(arn)
            .build()

        snsClient.publish(publishRequest)
    }

    fun registerWithSNS(token: String, previousEndpointArn: String?): String {
        var endpointArn = previousEndpointArn
        var updateNeeded = false
        var createNeeded = null == endpointArn

        if (createNeeded) {
            // No platform endpoint ARN is stored; need to call createEndpoint.
            endpointArn = createEndpoint(token)
            createNeeded = false
        }

        println("Retrieving platform endpoint data...")
        // Look up the platform endpoint and make sure the data in it is current, even if
        // it was just created.
        try {
            val geaReq = GetEndpointAttributesRequest.builder()
                .endpointArn(endpointArn)
                .build()
            val geaRes = snsClient.getEndpointAttributes(geaReq)

            updateNeeded = geaRes.attributes()["Token"] != token || geaRes.attributes()["Enabled"]?.toLowerCase() != "true"

        } catch (nfe: NotFoundException) {
            // We had a stored ARN, but the platform endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true
        }

        if (createNeeded) {
            createEndpoint(token)
        }

        println("updateNeeded = $updateNeeded")

        if (updateNeeded) {
            // The platform endpoint is out of sync with the current data;
            // update the token and enable it.
            println("Updating platform endpoint " + endpointArn!!)
            val attribs = HashMap<String, String>()
            attribs["Token"] = token
            attribs["Enabled"] = "true"
            val saeReq = SetEndpointAttributesRequest.builder()
                .endpointArn(endpointArn)
                .attributes(attribs)
                .build()
            snsClient.setEndpointAttributes(saeReq)
        }

        return endpointArn!!
    }

    /**
     * @return never null
     */
    private fun createEndpoint(token: String): String? {
        var endpointArn: String? = null
        try {
            println("Creating platform endpoint with token $token")
            val cpeReq = CreatePlatformEndpointRequest.builder()
                .platformApplicationArn(applicationArn)
                .token(token)
                .build()
            val cpeRes = snsClient
                .createPlatformEndpoint(cpeReq)
            endpointArn = cpeRes.endpointArn()
        } catch (ipe: InvalidParameterException) {
            val message = ipe.awsErrorDetails().errorMessage()
            println("Exception message: $message")
            val p = Pattern
                .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " + "with the same [Tt]oken.*")
            val m = p.matcher(message)
            if (m.matches()) {
                // The platform endpoint already exists for this token, but with
                // additional custom data that
                // createEndpoint doesn't want to overwrite. Just use the
                // existing platform endpoint.
                endpointArn = m.group(1)
            } else {
                // Rethrow the exception, the input is actually bad.
                throw ipe
            }
        }

        return endpointArn
    }
}