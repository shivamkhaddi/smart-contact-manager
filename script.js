console.log("this is script file")

const toggleSidebar = () => {
    const $sidebar = $(".sidebar");
    const $content = $(".content");

    if ($sidebar.is(":visible")) {
        $sidebar.hide();
        $content.css("margin-left", "0");
    } else {
        $sidebar.show();
        $content.css("margin-left", "20%");// Adjust with the appropriate width
    }
};


const search=()=>{
	//console.log("searching...");
	let query = $("#search-input").val();
	
	if(query==""){
		$(".search-result").hide();
	}else{
		//console.log(query);
		let url=`http://localhost:8085/search/${query}`
		
		fetch(url).then((response) =>{
			return response.json();
		})
		.then((data)=>{
			//console.log(data);
			
			let text=`<div class='list-group'>`
			
			data.forEach((contact)=> {
				text+=`<a href='#' class='list-group-item list-group-action'>${contact.name}</a>`
			});
			
			text+=`</div>`;
			
			$(".search-result").html(text);
			$(".search-result").show();
		});
		
		$(".search-result").show();
	}
};



//first request to server to create order

const paymentStart=()=>{
	console.log("payment started...");
	let amount = $("#payment_field").val();
	console.log(amount);

	if(amount == '' || amount==null){
		//alert("amount is required!!");
		swal("Failed !!", "amount is required !!", "error");
		return;
	}

	//code
	// we will use ajax to send request to server to create order...

	$.ajax(
		{
			url: '/user/create_order',
			data:JSON.stringify( {amount:amount,info:'order_request'}),
			contentType:'application/json',
			type:'POST',
			dataType:'json',
			success:function(response){
				//invoked when success
				console.log(response)
				if(response.status == "created" ){
					//open payment form

					let options={
						key:'rzp_test_xD7CjqrC6Eewrw',
						amount:response.amount,
						currency:'INR',
						name:'Smart Contact Manager',
						description:'Donation',
						image:'',
						order_id:response.id,
						handler:function(response){
							console.log(response.razorpay_payment_id)
							console.log(response.razorpay_order_id)
							console.log(response.razorpay_signature)
							console.log('payment successfull !!')
							
							
							updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
							
							
							
						},
						prefill: {
							name: "",
							email: "",
							contact: "",
						},

						notes:{
							address:"Learning payment integration",
						},

					theme:{
						color: "#3399cc",
					},	


						
					};
					let rzp = new Razorpay(options);

                    rzp.on("payment.failed", function (response){
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						
						swal("Failed !!", "oops, payment failed", "error");
					});

					rzp.open();
				}else{
					alert("order creation failed,[lease try again");
				}
			},
			error:function(error){
				//invoked when error
				console.log(error)
				alert("something went wrong !!")
			}
		}
	)
};


 function updatePaymentOnServer(payment_id,order_id, status)
{
	
	$.ajax({
		url: '/user/update_order',
					data:JSON.stringify( {payment_id: payment_id, order_id: order_id, status: status}),
					contentType:'application/json',
					type:'POST',
					dataType:'json',
					
					success:function(response){
						swal("Good job!", "congrats, payment successfull...", "success");
					},
					
					error: function(error){
						swal("Failed !!", "your payment is successful, but we did not  get on server, we will contact you as soon as possible", "error");
					},
	});
}