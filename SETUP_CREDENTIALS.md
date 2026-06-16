# Setting Up Email and SMS Credentials

## 1. Gmail Configuration (for Email OTP)

### Step 1: Enable 2-Factor Authentication
1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to **Security** → **2-Step Verification**
3. Enable 2-Step Verification if not already enabled

### Step 2: Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select **Mail** and **Other (Custom name)**
3. Enter "Sakny API" as the name
4. Click **Generate**
5. Copy the 16-character password (no spaces)

### Step 3: Update .env File
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx  # Replace with your app password
```

## 2. Twilio Configuration (for SMS OTP)

### Step 1: Create Twilio Account
1. Go to: https://www.twilio.com/try-twilio
2. Sign up for a free trial account
3. Verify your email and phone number

### Step 2: Get Credentials
1. Go to Twilio Console: https://console.twilio.com/
2. Find your **Account SID** and **Auth Token**
3. Get a phone number:
   - Go to **Phone Numbers** → **Manage** → **Buy a number**
   - Select a number with SMS capabilities
   - For trial accounts, you can only send to verified numbers

### Step 3: Update .env File
```bash
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_FROM_NUMBER=+1234567890  # Your Twilio phone number
```

## 3. Alternative: Development Mode (Skip Verification)

If you want to test without configuring email/SMS:

### Option A: Mock OTP Service (Recommended for Development)
You can temporarily disable actual OTP sending for testing.

### Option B: Skip Verification
Users can click "Skip for now" on the verification page.

## 4. After Configuration

1. Update the `.env` file with your credentials
2. Restart your Spring Boot application
3. The verification should now work

## Testing

### Test Email OTP:
```bash
curl -X POST http://localhost:8081/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "your-email@gmail.com",
    "channel": "EMAIL",
    "purpose": "REGISTRATION"
  }'
```

### Test SMS OTP:
```bash
curl -X POST http://localhost:8081/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "+1234567890",
    "channel": "PHONE",
    "purpose": "REGISTRATION"
  }'
```

## Common Issues

### Gmail "Less secure app access"
- Gmail no longer supports "less secure apps"
- You MUST use App Passwords (see Step 2 above)

### Twilio Trial Account Limitations
- Can only send SMS to verified numbers
- Add test numbers in Twilio Console → **Phone Numbers** → **Verified Caller IDs**

### Authentication Failed Error
- Check that credentials are correctly set in `.env`
- Verify the Spring Boot app loaded the environment variables
- Check application logs for detailed error messages
