package ir.tahamohamadi.content.contact.api.publicsite;
import ir.tahamohamadi.content.contact.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service @ConditionalOnExpression("!'${spring.autoconfigure.exclude:}'.contains('DataSourceAutoConfiguration')")
public class PublicContactService {
    private final ContactMessageRepository messages; private final Map<String,Deque<Instant>> submissions=new ConcurrentHashMap<>();
    public PublicContactService(ContactMessageRepository messages){this.messages=messages;}
    @Transactional public ContactSubmissionResponse submit(ContactSubmissionRequest request,String remoteAddress){limit(remoteAddress);messages.save(ContactMessage.submit(UUID.randomUUID(),request.name(),request.email(),request.message(),request.language(),Instant.now()));return new ContactSubmissionResponse("accepted");}
    private void limit(String address){Instant threshold=Instant.now().minus(Duration.ofHours(1));Deque<Instant> attempts=submissions.computeIfAbsent(address==null?"unknown":address,k->new ArrayDeque<>());synchronized(attempts){while(!attempts.isEmpty()&&attempts.peekFirst().isBefore(threshold))attempts.removeFirst();if(attempts.size()>=5)throw new IllegalStateException("Too many submissions");attempts.addLast(Instant.now());}}
}
