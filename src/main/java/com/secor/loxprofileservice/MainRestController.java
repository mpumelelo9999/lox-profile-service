package com.secor.loxprofileservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("api/v1")
public class MainRestController
{

    private static final Logger log = LoggerFactory.getLogger(MainRestController.class);


    UserdetailRepository userdetailRepository;
    WebClient webClient_1;

    MainRestController(UserdetailRepository userdetailRepository,
                       WebClient webClient_1)
    {
        this.userdetailRepository = userdetailRepository;
        this.webClient_1 = webClient_1;
    }

    @PostMapping("update/user/details")
    public ResponseEntity<String> updateUserDetails(
            @RequestHeader("Sectoken") String secToken,
            @RequestBody Userdetail userdetail)
    {
        // CHECK FOR THE SECRET COOKIE TO DECIDE WHETHER THIS IS A FRESH REQUEST OR A FOLLOW-UP REQUEST

         AtomicReference<String> username = new AtomicReference<>(null);

        if(secToken == null || secToken.isEmpty())
        {
            return ResponseEntity.badRequest().body(null);
        }
        else
        {
            // Send the token for validation to Auth-Service in an Async manner

            Mono<Authtoken> responseAuth = webClient_1.post().header("Sectoken",secToken)
                    .retrieve()
                    .bodyToMono(Authtoken.class); // SENDING OUT AN ASYNCHRONOUS REQUEST

            // THE CODE ABOVE AND BELOW WILL BE EXECUTED AT DIFFERENT TIMES AND IN SEPERATE THREADS

            responseAuth.subscribe( // HANDLER FOR THE EVENTUAL RESPONSE TO THE ABOVE REQUEST
                    response -> {
                        log.info(response+" VALID TOKEN RESPONSE FROM AUTH-SERVICE");
                        username.set(response.getUsername());
                        //redisTemplate.opsForValue().set(credential.getCitizenid().toString(), credential.getPassword());

                        if(username.equals(userdetail.getUsername()) && !(userdetail.getUsername() == null || userdetail.getUsername().isEmpty()))
                        {
                            if(userdetailRepository.findById(username.get()).isEmpty())
                            {
                                userdetailRepository.save(userdetail);
                            }
                            else
                            {
                                userdetailRepository.updateFirstnameAndLastnameAndEmailAndPhoneAndCityAndCountryByUsername(
                                        userdetail.getFirstname(),
                                        userdetail.getLastname(),
                                        userdetail.getEmail(),
                                        userdetail.getPhone(),
                                        userdetail.getCity(),
                                        userdetail.getCountry(),
                                        username.get()
                                );
                            }
                        }


                    },
                    error ->
                    {
                        log.info("INVALID TOKEN RESPONSE "+error);
                    });

           return ResponseEntity.ok().body("AUTHENTICATING REQUEST... PLEASE CHECK AGAIN MR FRONTEND");

        }

    }



}
